package com.patika.getir_lite.data

import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProductRepository {
    val dataSyncResult: StateFlow<List<DataSyncResult>>
    fun getProductsAsFlow(): Flow<List<Product>>
    suspend fun syncWithRemote(): BaseResponse<Unit>
    fun getSuggestedProductsAsFlow(): Flow<List<Product>>
    fun getBasketAsFlow(): Flow<Order?>
    suspend fun updateItemCount(productId: Long, count: Int)
}
