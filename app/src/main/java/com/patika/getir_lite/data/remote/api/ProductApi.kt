package com.patika.getir_lite.data.remote.api

import com.patika.getir_lite.data.remote.model.ProductContainerDto
import retrofit2.Response
import retrofit2.http.GET

interface ProductApi {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductContainerDto>>
}
