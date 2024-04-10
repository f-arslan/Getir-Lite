package com.patika.getir_lite.data

import com.patika.getir_lite.data.remote.model.DataResult
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.SuggestedProduct

interface ProductRepository {
    suspend fun getProducts(): DataResult<List<Product>>
    suspend fun getSuggestedProducts(): DataResult<List<SuggestedProduct>>
}
