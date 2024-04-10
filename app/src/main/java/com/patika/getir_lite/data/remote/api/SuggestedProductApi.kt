package com.patika.getir_lite.data.remote.api

import com.patika.getir_lite.data.remote.model.SuggestedProductContainerDto
import retrofit2.Response
import retrofit2.http.GET

interface SuggestedProductApi {
    @GET("suggestedProducts")
    suspend fun getSuggestedProducts(): Response<List<SuggestedProductContainerDto>>
}
