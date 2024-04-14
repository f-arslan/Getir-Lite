package com.patika.getir_lite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.model.ProductType
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProduct(productEntity: ProductEntity)

    @Insert
    suspend fun insertOrder(orderEntity: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(productEntities: List<ProductEntity>)

    @Query("SELECT * FROM items WHERE productType = :productType")
    fun getAllItemsByType(productType: ProductType): Flow<List<ProductEntity>>

    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<ProductEntity>

    @Query("UPDATE items SET orderId = :orderId, count = count + :count WHERE id = :productId")
    suspend fun updateItemCountOrder(productId: Long, count: Int, orderId: Long)

    @Query("SELECT * FROM items WHERE id = :productId")
    suspend fun getProductById(productId: Long): ProductEntity

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): OrderEntity

    @Query("SELECT * FROM orders WHERE orderStatus = :status")
    suspend fun getActiveOrder(status: OrderStatus): OrderEntity?

    @Query("UPDATE orders SET totalPrice = totalPrice + :price WHERE id = :orderId")
    suspend fun updateActiveOrderPrice(orderId: Long, price: Double)

    @Transaction
    suspend fun updateItem(productId: Long, count: Int) {
        val product = getProductById(productId)
        if (product.count == 0 && count < 0) return

        val getActiveOrder = getActiveOrder(OrderStatus.ON_BASKET)

        val orderId = getActiveOrder?.id ?: run {
            val id = insertOrder(
                OrderEntity(orderStatus = OrderStatus.ON_BASKET)
            )
            id
        }

        updateItemCountOrder(productId, count, orderId)

        when {
            count < 0 -> updateActiveOrderPrice(orderId, -product.price.toDouble())
            else -> updateActiveOrderPrice(orderId, product.price.toDouble())
        }

        if (count + product.count == 0) {
            updateItemCountOrder(productId = productId, count = 0, orderId = -1)
        }
    }
}
