package com.patika.getir_lite.fake

import com.patika.getir_lite.data.DataSyncResult
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.ItemWithProduct
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.toDomainModel
import com.patika.getir_lite.fake.data.fakeProductWithCounts
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.BasketWithProducts
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.ProductWithCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal

class FakeProductDataSource : ProductRepository {
    private val localProducts = fakeProductWithCounts.toMutableList()
    val orders = mutableListOf<OrderEntity>()
    private val items = mutableListOf<ItemEntity>()

    private val _dataSyncResult = MutableStateFlow<List<DataSyncResult>>(emptyList())
    override val dataSyncResult: StateFlow<List<DataSyncResult>> = _dataSyncResult

    override suspend fun syncWithRemote(): BaseResponse<Unit> {
        return BaseResponse.Success(Unit)
    }

    override fun getProductsAsFlow(): Flow<List<ProductWithCount>> = flow {
        emit(localProducts.filter { it.productType == ProductType.PRODUCT })
    }

    override fun getSuggestedProductsAsFlow(): Flow<List<ProductWithCount>> = flow {
        emit(localProducts.filter { it.productType == ProductType.SUGGESTED_PRODUCT })
    }

    override fun getBasketAsFlow(): Flow<Order?> = flow {
        emit(orders.firstOrNull { it.orderStatus == OrderStatus.ON_BASKET }?.toDomainModel())
    }

    fun ProductWithCount.toProductEntity(): ProductEntity {
        return ProductEntity(
            id = productId,
            productId = "",
            name = name,
            price = price,
            attribute = attribute ?: "",
            imageURL = imageURL,
            productType = productType,
        )
    }

    override fun getBasketWithProductsAsFlow(): Flow<BasketWithProducts?> = flow {
        val order = orders.firstOrNull { it.orderStatus == OrderStatus.ON_BASKET }
            ?: return@flow emit(null)
        val items = items.filter { it.orderId == order.id }
        val products = localProducts.filter { product ->
            items.any { it.productId == product.productId }
        }
        val itemWithProducts = items.map { item ->
            val product = products.find { it.productId == item.productId }
                ?: throw Exception("Product not found")
            ItemWithProduct(item, product.toProductEntity())
        }
        emit(BasketWithProducts(order, itemWithProducts))
    }

    override fun getProductAsFlow(productId: Long): Flow<ProductWithCount?> = flow {
        emit(localProducts.find { it.productId == productId })
    }

    private fun getItemByProductAndOrder(productId: Long, orderId: Long): ItemEntity? =
        items.find { it.productId == productId && it.orderId == orderId }

    private fun addItemToBasket(productId: Long, orderId: Long, price: BigDecimal) {
        val itemEntity = getItemByProductAndOrder(productId, orderId)

        itemEntity?.let {
            val c = itemEntity.count + 1
            val updatedItem = itemEntity.copy(count = c)
            val index = items.indexOf(itemEntity)
            items[index] = updatedItem
        } ?: run {
            val item = ItemEntity(
                productId = productId,
                orderId = orderId,
                count = 1
            )
            items.add(item)
        }

        val order = orders.find { it.id == orderId } ?: throw Exception("Order not found")
        val updatedOrder =
            order.copy(totalPrice = (order.totalPrice.toDouble() + price.toDouble()).toBigDecimal())
        val orderIndex = orders.indexOf(order)
        orders[orderIndex] = updatedOrder

        val product =
            localProducts.find { it.productId == productId } ?: throw Exception("Product not found")
        val updatedProduct = product.copy(count = product.count + 1)
        val productIndex = localProducts.indexOf(product)
        localProducts[productIndex] = updatedProduct
    }

    private fun decrementItemCount(productId: Long, orderId: Long, price: BigDecimal) {
        val itemEntity =
            getItemByProductAndOrder(productId, orderId) ?: throw Exception("Item not found")
        val c = itemEntity.count - 1
        val updatedItem = itemEntity.copy(count = c)
        val index = items.indexOf(itemEntity)
        items[index] = updatedItem

        if (c <= 0) {
            items.removeAt(index)
        }

        val order = orders.find { it.id == orderId } ?: throw Exception("Order not found")
        val updatedOrder =
            order.copy(totalPrice = (order.totalPrice.toDouble() - price.toDouble()).toBigDecimal())
        val orderIndex = orders.indexOf(order)
        orders[orderIndex] = updatedOrder

        val product =
            localProducts.find { it.productId == productId } ?: throw Exception("Product not found")
        val updatedProduct = product.copy(count = product.count - 1)
        val productIndex = localProducts.indexOf(product)
        localProducts[productIndex] = updatedProduct
    }

    override suspend fun updateItemCount(productId: Long, countType: CountType) {
        val getActiveOrder = orders.firstOrNull { it.orderStatus == OrderStatus.ON_BASKET }
        val productPrice = localProducts.find { it.productId == productId }?.price
            ?: throw Exception("Product not found")
        val orderId = getActiveOrder?.id ?: run {
            val id = (0..100000).random().toLong()
            val orderEntity = OrderEntity(id = id, OrderStatus.ON_BASKET)
            orders.add(orderEntity)
            id
        }

        when (countType) {
            CountType.PLUS_ONE -> addItemToBasket(productId, orderId, productPrice)
            CountType.MINUS_ONE -> decrementItemCount(productId, orderId, productPrice)
        }
    }

    override suspend fun clearBasket(): Boolean {
        val order = orders.firstOrNull { it.orderStatus == OrderStatus.ON_BASKET }
            ?: return false

        localProducts.forEach { product ->
            val updatedProduct = product.copy(count = 0)
            val productIndex = localProducts.indexOf(product)
            localProducts[productIndex] = updatedProduct
        }

        items.removeAll { it.orderId == order.id }


        val updatedOrder =
            order.copy(totalPrice = 0.0.toBigDecimal(), orderStatus = OrderStatus.FINISHED)
        val orderIndex = orders.indexOf(order)
        orders[orderIndex] = updatedOrder

        return true
    }
}
