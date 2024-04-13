package com.patika.getir_lite.data

import com.patika.getir_lite.data.remote.model.DataResult
import com.patika.getir_lite.model.Product

interface ProductRepository {
    suspend fun getProducts(): DataResult<List<Product>>
    suspend fun getSuggestedProducts(): DataResult<List<Product>>
}
