package com.patika.getir_lite.model

import com.patika.getir_lite.data.local.model.ProductEntity
import java.math.BigDecimal

data class Product(
    val id: String,
    val entityId: Long = -1,
    val orderId: Long = -1,
    val name: String,
    val price: BigDecimal,
    val attribute: String,
    val imageURL: String? = null,
    val count: Int = 0,
    val productType: ProductType = ProductType.PRODUCT,
)

fun Product.toItemEntity() = ProductEntity(
    productId = id,
    orderId = orderId,
    name = name,
    price = price,
    attribute = attribute,
    imageURL = imageURL,
    count = count,
    productType = productType,
)
