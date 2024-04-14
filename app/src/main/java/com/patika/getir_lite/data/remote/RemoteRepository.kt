package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.model.BaseResponse

interface RemoteRepository {
    suspend fun getProductDtos(): BaseResponse<List<ProductDto>>
    suspend fun getSuggestedProductDtos(): BaseResponse<List<SuggestedProductDto>>
}
