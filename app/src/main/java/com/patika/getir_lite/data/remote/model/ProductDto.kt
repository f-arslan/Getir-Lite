package com.patika.getir_lite.data.remote.model

import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.model.ProductType
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: String,
    val name: String,
    val attribute: String? = null,
    val thumbnailURL: String,
    val imageURL: String,
    val price: Double,
    val priceText: String,
    val shortDescription: String? = null
)

fun ProductDto.toProductEntity(): ProductEntity {
    val attribute = attribute ?: shortDescription ?: ""
    return ProductEntity(
        productId = id,
        name = name.trim(),
        price = price.toBigDecimal(),
        attribute = attribute.trim(),
        imageURL = imageURL,
        productType = ProductType.PRODUCT
    )
}
