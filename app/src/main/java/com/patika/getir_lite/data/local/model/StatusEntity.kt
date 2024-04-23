package com.patika.getir_lite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("status")
data class StatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isProductLoaded: Boolean = false,
    val isSuggestedProductLoaded: Boolean = false
)
