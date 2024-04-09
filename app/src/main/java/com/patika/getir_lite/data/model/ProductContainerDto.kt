package com.patika.getir_lite.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ProductContainerDto(
    val id: String,
    val name: String,
    val productCount: Int,
    val productDtos: List<ProductDto>
)
