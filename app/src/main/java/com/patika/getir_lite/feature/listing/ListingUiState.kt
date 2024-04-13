package com.patika.getir_lite.feature.listing

import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.Response

data class ListingRemoteUiState(
    val product: Response<List<Product>> = Response.Loading,
    val suggestedProduct: Response<List<Product>> = Response.Loading
)
