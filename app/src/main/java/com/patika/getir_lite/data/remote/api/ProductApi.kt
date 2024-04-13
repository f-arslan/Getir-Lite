package com.patika.getir_lite.data.remote.api

import com.patika.getir_lite.data.remote.model.ProductContainerDto
import com.patika.getir_lite.data.remote.model.SuggestedProductContainerDto
import retrofit2.Response
import retrofit2.http.GET

interface ProductApi {
    @GET("products")
    suspend fun getProducts(): Response<List<ProductContainerDto>>

    @GET("suggestedProducts")
    suspend fun getSuggestedProducts(): Response<List<SuggestedProductContainerDto>>
}
