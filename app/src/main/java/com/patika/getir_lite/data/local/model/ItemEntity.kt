package com.patika.getir_lite.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            childColumns = ["orderId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            childColumns = ["productId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["productId"])
    ]
)

data class ItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val orderId: Long,
    val count: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
