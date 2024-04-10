package com.patika.getir_lite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductContainerDto(
    val id: String,
    val name: String,
    val productCount: Int,
    @SerialName("products")
    val productDtos: List<ProductDto>
)
