package com.patika.getir_lite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.model.BasketWithProducts
import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.ProductWithCount
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface ProductDao {
    @Insert
    suspend fun insertOrder(orderEntity: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(productNEntities: List<ProductEntity>)

    @Query(
        """
    SELECT p.id as productId, p.name, p.price, p.attribute, p.imageURL, p.productType, IFNULL(SUM(i.count), 0) as count
    FROM products p
    LEFT JOIN items i ON p.id = i.productId 
    AND i.orderId = (SELECT o.id FROM orders o WHERE o.orderStatus = :orderStatus ORDER BY o.id DESC LIMIT 1)
    WHERE p.productType = :productType
    GROUP BY p.id
    """
    )
    fun getProductsWithCounts(
        productType: ProductType,
        orderStatus: OrderStatus = OrderStatus.ON_BASKET
    ): Flow<List<ProductWithCount>>

    @Query(
        """
    SELECT p.id as productId, p.name, p.price, p.attribute, p.imageURL, p.productType, IFNULL(SUM(i.count), 0) as count
    FROM products p
    LEFT JOIN items i ON p.id = i.productId
    AND i.orderId = (SELECT o.id FROM orders o WHERE o.orderStatus = :orderStatus ORDER BY o.id DESC LIMIT 1)
    WHERE p.id = :productId
    GROUP BY p.id
    """
    )
    fun getProductWithCount(
        productId: Long,
        orderStatus: OrderStatus = OrderStatus.ON_BASKET
    ): Flow<ProductWithCount?>

    @Query("SELECT * FROM products")
    suspend fun getAllItems(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Long): ProductEntity

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Long): OrderEntity

    @Query("SELECT * FROM orders WHERE orderStatus = :status")
    suspend fun getActiveOrder(status: OrderStatus): OrderEntity?

    @Query("SELECT * FROM orders WHERE orderStatus = :status")
    fun getActiveOrderAsFlow(status: OrderStatus): Flow<OrderEntity?>

    @Query("UPDATE orders SET totalPrice = totalPrice + :price WHERE id = :orderId")
    suspend fun updateActiveOrderPrice(orderId: Long, price: BigDecimal)

    @Query("UPDATE items SET orderId = -1, count = 0 WHERE orderId = :orderId")
    suspend fun clearBasket(orderId: Long)

    @Query("UPDATE orders SET totalPrice = 0, orderStatus = :status WHERE id = :orderId")
    suspend fun finishOrder(orderId: Long, status: OrderStatus = OrderStatus.FINISHED)


    @Query("SELECT * FROM items WHERE productId = :productId AND orderId = :orderId")
    suspend fun getItemByProductAndOrder(productId: Long, orderId: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItem(item: ItemEntity): Long

    @Update
    fun updateItem(item: ItemEntity)

    @Transaction
    suspend fun addItemToBasket(productId: Long, orderId: Long, price: BigDecimal) {
        val itemEntity = getItemByProductAndOrder(productId, orderId)

        itemEntity?.let {
            val c = itemEntity.count + 1
            val updatedItem = itemEntity.copy(count = c)
            updateItem(updatedItem)
        } ?: run {
            insertItem(ItemEntity(productId = productId, orderId = orderId, count = 1))
        }

        updateActiveOrderPrice(orderId, price)
    }

    @Query("UPDATE items SET count = count - 1 WHERE productId = :productId AND orderId = :orderId AND count > 0")
    fun decrementItemCount(productId: Long, orderId: Long)

    @Query("DELETE FROM items WHERE productId = :productId AND orderId = :orderId AND count <= 0")
    fun cleanupZeroCountItems(productId: Long, orderId: Long)

    @Transaction
    suspend fun decrementItemCount(productId: Long, orderId: Long, price: BigDecimal) {
        decrementItemCount(productId, orderId)
        cleanupZeroCountItems(productId, orderId)
        updateActiveOrderPrice(orderId, -price)
    }

    @Transaction
    suspend fun cancelOrder() {
        val order = getActiveOrder(OrderStatus.ON_BASKET) ?: return
        clearBasket(order.id)
        finishOrder(order.id)
    }

    @Transaction
    @Query("SELECT * FROM orders WHERE orderStatus = :orderStatus")
    fun getBasketWithProducts(orderStatus: OrderStatus): Flow<BasketWithProducts?>
}
