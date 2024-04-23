package com.patika.getir_lite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.BasketWithProducts
import com.patika.getir_lite.data.local.model.StatusEntity
import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.ProductType.*
import com.patika.getir_lite.model.ProductWithCount
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

@Dao
interface ProductDao {
    @Insert
    suspend fun insertOrder(orderEntity: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(productEntities: List<ProductEntity>)

    @Query(
        """
    SELECT p.id as productId, p.name, p.price, p.attribute, p.imageURL, p.productType as type, IFNULL(SUM(i.count), 0) as count
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
    SELECT p.id as productId, p.name, p.price, p.attribute, p.imageURL, p.productType as type, IFNULL(SUM(i.count), 0) as count
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

    @Query("UPDATE orders SET totalPrice = 0, orderStatus = :status WHERE id = :orderId")
    suspend fun finishOrder(orderId: Long, status: OrderStatus = OrderStatus.FINISHED)


    @Query("SELECT * FROM items WHERE productId = :productId AND orderId = :orderId")
    suspend fun getItemByProductAndOrder(productId: Long, orderId: Long): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertItem(item: ItemEntity): Long

    @Update
    fun updateItem(item: ItemEntity)

    @Insert
    fun insertStatus(status: StatusEntity): Long

    @Query("UPDATE status SET isSuggestedProductLoaded = :isSuggestedProductLoaded")
    fun updateSuggestedProductState(isSuggestedProductLoaded: Boolean)

    @Query("UPDATE status SET isProductLoaded = :isProductLoaded")
    fun updateProductState(isProductLoaded: Boolean)

    @Query("SELECT * FROM status")
    suspend fun getStatus(): List<StatusEntity>

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

    @Transaction
    suspend fun insertProductsFirstTime(data: List<ProductEntity>, productType: ProductType) {
        when (productType) {
            PRODUCT -> {
                insertProducts(data)
                updateProductState(true)
            }

            SUGGESTED_PRODUCT -> {
                insertProducts(data)
                updateSuggestedProductState(true)
            }
        }
    }

    @Query("UPDATE items SET count = count - 1 WHERE productId = :productId AND orderId = :orderId AND count > 0")
    fun decrementItemCount(productId: Long, orderId: Long)

    @Query("DELETE FROM items WHERE productId = :productId AND orderId = :orderId AND count <= 0")
    fun cleanupZeroCountItems(productId: Long, orderId: Long)

    @Transaction
    suspend fun decrementItemCount(productId: Long, orderId: Long, price: BigDecimal) {
        val itemEntity = getItemByProductAndOrder(productId, orderId) ?: return
        if (itemEntity.count <= 0) return

        decrementItemCount(productId, orderId)
        cleanupZeroCountItems(productId, orderId)
        updateActiveOrderPrice(orderId, -price)
    }

    @Transaction
    suspend fun cancelOrder() {
        val order = getActiveOrder(OrderStatus.ON_BASKET) ?: return
        finishOrder(order.id)
    }

    @Transaction
    @Query("SELECT * FROM orders WHERE orderStatus = :orderStatus")
    fun getBasketWithProducts(orderStatus: OrderStatus): Flow<BasketWithProducts?>
}
