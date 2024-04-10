package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.data.remote.model.DataResult

interface RemoteRepository {
    suspend fun getProductDtos(): DataResult<List<ProductDto>>
    suspend fun getSuggestedProductDtos(): DataResult<List<SuggestedProductDto>>
}
