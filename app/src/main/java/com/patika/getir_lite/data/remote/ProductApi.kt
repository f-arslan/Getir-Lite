package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.model.ProductContainerDto
import retrofit2.Response
import retrofit2.http.GET

interface ProductApi {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductContainerDto>>
}
