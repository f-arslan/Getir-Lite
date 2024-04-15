package com.patika.getir_lite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.patika.getir_lite.model.Product
import com.patika.getir_lite.model.ProductType
import java.math.BigDecimal

@Entity(tableName = "items")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long = -1,
    val productId: String,
    val name: String,
    val price: BigDecimal,
    val attribute: String,
    val imageURL: String? = null,
    val count: Int = 0,
    val productType: ProductType = ProductType.PRODUCT,
)

fun ProductEntity.toDomainModel() = Product(
    entityId = id,
    id = productId,
    orderId = orderId,
    name = name,
    price = price,
    attribute = attribute,
    imageURL = imageURL,
    count = count,
    productType = productType
)
