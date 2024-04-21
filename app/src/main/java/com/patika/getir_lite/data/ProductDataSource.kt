package com.patika.getir_lite.data

import android.util.Log
import com.patika.getir_lite.data.di.AppDispatchers.IO
import com.patika.getir_lite.data.di.Dispatcher
import com.patika.getir_lite.data.local.ProductDao
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.toDomainModel
import com.patika.getir_lite.data.remote.RemoteRepository
import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.data.remote.model.toProductEntity
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.BasketWithProducts
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.CountType.MINUS_ONE
import com.patika.getir_lite.model.CountType.PLUS_ONE
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.TopLevelException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductDataSource @Inject constructor(
    private val remoteRepository: RemoteRepository,
    private val productDao: ProductDao,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {

    private val _dataSyncResult = MutableStateFlow(listOf(DataSyncResult.IDLE))
    override val dataSyncResult = _dataSyncResult.asStateFlow()

    override fun getProductsAsFlow(): Flow<List<ProductWithCount>> =
        productDao.getProductsWithCounts(ProductType.PRODUCT)

    override fun getSuggestedProductsAsFlow(): Flow<List<ProductWithCount>> =
        productDao.getProductsWithCounts(ProductType.SUGGESTED_PRODUCT)

    override suspend fun syncWithRemote(): BaseResponse<Unit> {
        return try {
            withContext(ioDispatcher) {
                val remoteProductsDeferred = async { getProducts() }
                val remoteSuggestedProductsDeferred = async { getSuggestedProducts() }
                val localProductsDeferred = async { productDao.getAllItems() }

                val remoteProducts = remoteProductsDeferred.await()
                val remoteSuggestedProducts = remoteSuggestedProductsDeferred.await()
                val localProduct = localProductsDeferred.await()

                if (localProduct.isEmpty()) {
                    productDao.insertOrder(OrderEntity(-1))
                    if (remoteProducts is BaseResponse.Success) {
                        saveDataToLocalFirstTime(remoteProducts.data)
                        _dataSyncResult.update { it + DataSyncResult.PRODUCT_SYNCED }
                    }

                    if (remoteSuggestedProducts is BaseResponse.Success) {
                        saveDataToLocalFirstTime(remoteSuggestedProducts.data)
                        _dataSyncResult.update { it + DataSyncResult.SUGGESTED_PRODUCT_SYNCED }
                    }
                }

                BaseResponse.Success(Unit)
            }
        } catch (e: Exception) {
            BaseResponse.Error(TopLevelException.GenericException(e.message))
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

    private suspend fun saveDataToLocalFirstTime(products: List<ProductEntity>) {
        productDao.insertProducts(products)
    }

    override suspend fun updateItemCount(productId: Long, countType: CountType) = try {
        withContext(ioDispatcher) {
            val getActiveOrder = productDao.getActiveOrder(OrderStatus.ON_BASKET)
            val productPrice = productDao.getProductById(productId).price
            val orderId = getActiveOrder?.id ?: run {
                val id = productDao.insertOrder(
                    OrderEntity(orderStatus = OrderStatus.ON_BASKET)
                )
                id
            }

            when (countType) {
                PLUS_ONE -> productDao.addItemToBasket(productId, orderId, productPrice)
                MINUS_ONE -> productDao.decrementItemCount(productId, orderId, productPrice)
            }
        }
    } catch (e: Exception) {
        Log.e("ProductDataSource", "updateItemCount: $e")
        Unit
    }

    override suspend fun clearBasket(): Boolean = try {
        withContext(ioDispatcher) {
            productDao.cancelOrder()
            true
        }
    } catch (e: Exception) {
        false
    }
}

enum class DataSyncResult {
    PRODUCT_SYNCED, SUGGESTED_PRODUCT_SYNCED, IDLE
}
