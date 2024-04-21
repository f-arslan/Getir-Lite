package com.patika.getir_lite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.patika.getir_lite.model.ProductType
import java.math.BigDecimal

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: String,
    val name: String,
    val price: BigDecimal,
    val attribute: String,
    val imageURL: String? = null,
    val productType: ProductType,
)
