package com.patika.getir_lite.data

import android.content.Context
import com.patika.getir_lite.data.di.AppDispatchers.IO
import com.patika.getir_lite.data.di.Dispatcher
import com.patika.getir_lite.data.local.ProductDao
import com.patika.getir_lite.data.local.di.DatabaseModule
import com.patika.getir_lite.data.local.model.BasketWithProducts
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.StatusEntity
import com.patika.getir_lite.data.local.model.toDomainModel
import com.patika.getir_lite.data.remote.RemoteRepository
import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.data.remote.model.toProductEntity
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.CountType.MINUS_ONE
import com.patika.getir_lite.model.CountType.PLUS_ONE
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.TopLevelException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * A data source class that handles fetching, caching, and retrieving products and orders from both local and remote sources.
 * This class serves as an implementation of the [ProductRepository] to provide an interface for data operations.
 *
 * @property remoteRepository The backend API repository used for fetching products from a remote server.
 * @property productDao The local DAO (Data Access Object) used for querying and updating the local database.
 * @property ioDispatcher A [CoroutineDispatcher] specifically for I/O operations to ensure database and network operations do not block the main thread.
 */
class ProductDataSource @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val productDao: ProductDao,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context
) : ProductRepository {
    override fun getProductsAsFlow(): Flow<List<ProductWithCount>> =
        productDao.getProductsWithCounts(ProductType.PRODUCT)

    override fun getSuggestedProductsAsFlow(): Flow<List<ProductWithCount>> =
        productDao.getProductsWithCounts(ProductType.SUGGESTED_PRODUCT)

    private val mutex = Mutex()

    override suspend fun fetchDataFromRemoteAndSync(isCached: Boolean): BaseResponse<Unit> = try {
        mutex.withLock {
            if (!isCached) {
                DatabaseModule.resetDatabase(context)
            }

            withContext(ioDispatcher) {
                val dbStatus = productDao.getStatus().firstOrNull() ?: run {
                    val id = productDao.insertStatus(StatusEntity())
                    StatusEntity(id = id)
                }

                if (dbStatus.isProductLoaded && dbStatus.isSuggestedProductLoaded)
                    return@withContext BaseResponse.Success(Unit)

                if (!dbStatus.isProductLoaded) {
                    launch { insertProductToDb() }
                }

                if (!dbStatus.isSuggestedProductLoaded) {
                    launch { insertSuggestedProductToDb() }
                }

                BaseResponse.Success(Unit)
            }
        }
    } catch (e: Exception) {
        BaseResponse.Error(TopLevelException.GenericException(e.message))
    }

    private suspend fun insertProductToDb() {
        when (val remoteProducts = getProducts()) {
            is BaseResponse.Error -> throw remoteProducts.exception
            BaseResponse.Loading -> Unit
            is BaseResponse.Success -> {
                productDao.insertProductsFirstTime(
                    data = remoteProducts.data,
                    productType = ProductType.PRODUCT
                )
            }
        }
    }

    private suspend fun insertSuggestedProductToDb() {
        when (val remoteSuggestedProducts = getSuggestedProducts()) {
            is BaseResponse.Error -> throw remoteSuggestedProducts.exception
            BaseResponse.Loading -> Unit
            is BaseResponse.Success -> {
                productDao.insertProductsFirstTime(
                    data = remoteSuggestedProducts.data,
                    productType = ProductType.SUGGESTED_PRODUCT
                )
            }
        }
    }

    override fun getBasketAsFlow(): Flow<Order?> =
        productDao.getActiveOrderAsFlow(OrderStatus.ON_BASKET).map { it?.toDomainModel() }

    override fun getProductAsFlow(productId: Long): Flow<ProductWithCount?> =
        productDao.getProductWithCount(productId)

    override fun getBasketWithProductsAsFlow(): Flow<BasketWithProducts?> =
        productDao.getBasketWithProducts(OrderStatus.ON_BASKET)

    private suspend fun getProducts(): BaseResponse<List<ProductEntity>> =
        when (val response = remoteRepository.getProductDtos()) {
            is BaseResponse.Success -> {
                val products = response.data.map(ProductDto::toProductEntity)

                BaseResponse.Success(products)
            }

            is BaseResponse.Error -> response
            BaseResponse.Loading -> BaseResponse.Loading
        }

    private suspend fun getSuggestedProducts(): BaseResponse<List<ProductEntity>> =
        when (val response = remoteRepository.getSuggestedProductDtos()) {
            is BaseResponse.Success -> {
                val products = response.data.map(SuggestedProductDto::toProductEntity)

                BaseResponse.Success(products)
            }

            is BaseResponse.Error -> response
            BaseResponse.Loading -> BaseResponse.Loading
        }

    override suspend fun updateItemCount(productId: Long, countType: CountType) = runCatching {
        withContext(ioDispatcher) {
            val getActiveOrder = async { productDao.getActiveOrder(OrderStatus.ON_BASKET) }
            val productPrice = async { productDao.getProductById(productId).price }
            val orderId = getActiveOrder.await()?.id ?: run {
                val id = productDao.insertOrder(
                    OrderEntity(orderStatus = OrderStatus.ON_BASKET)
                )
                id
            }

            when (countType) {
                PLUS_ONE -> productDao.addItemToBasket(productId, orderId, productPrice.await())
                MINUS_ONE -> productDao.decrementItemCount(productId, orderId, productPrice.await())
            }
        }
    }

    override suspend fun getStatus(): List<StatusEntity?> = productDao.getStatus()

    override suspend fun clearBasket(): Boolean = try {
        withContext(ioDispatcher) {
            productDao.cancelOrder()
            true
        }
    } catch (e: Exception) {
        false
    }
}
