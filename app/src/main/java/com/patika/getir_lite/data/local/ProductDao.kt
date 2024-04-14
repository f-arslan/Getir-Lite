package com.patika.getir_lite.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.OrderStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insertItem(itemEntity: ItemEntity): Long

    @Insert
    suspend fun insertOrder(orderEntity: OrderEntity): Long

    @Query("UPDATE items SET count = count + :count WHERE id = :productId")
    suspend fun updateItemCount(productId: Long, count: Int)

    @Query("SELECT * FROM orders WHERE orderStatus = :status")
    fun getOrderByStatus(status: OrderStatus): Flow<OrderEntity>
}
