package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.remote.model.ApiResult
import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto

interface ProductRepository {
    suspend fun getProducts(): ApiResult<List<ProductDto>>
    suspend fun getSuggestedProducts(): ApiResult<List<SuggestedProductDto>>
}
