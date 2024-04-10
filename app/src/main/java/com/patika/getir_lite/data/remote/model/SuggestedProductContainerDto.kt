package com.patika.getir_lite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SuggestedProductContainerDto(
    val id: String,
    val name: String,
    @SerialName("products")
    val suggestedProductDtos: List<SuggestedProductDto>,
)
