package com.patika.getir_lite.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SuggestedProductContainerDto(
    val suggestedProductDtos: List<SuggestedProductDto>,
    val id: String,
    val name: String
)
