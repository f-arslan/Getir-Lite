package com.patika.getir_lite.data

import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.BasketWithProducts
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductWithCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ProductRepository {
    val dataSyncResult: StateFlow<List<DataSyncResult>>
    fun getProductsAsFlow(): Flow<List<ProductWithCount>>
    suspend fun syncWithRemote(): BaseResponse<Unit>
    fun getSuggestedProductsAsFlow(): Flow<List<ProductWithCount>>
    fun getBasketAsFlow(): Flow<Order?>
    fun getBasketWithProductsAsFlow(): Flow<BasketWithProducts?>
    fun getProductAsFlow(productId: Long): Flow<ProductWithCount?>
    suspend fun updateItemCount(productId: Long, countType: CountType)
    suspend fun clearBasket(): Boolean
}
