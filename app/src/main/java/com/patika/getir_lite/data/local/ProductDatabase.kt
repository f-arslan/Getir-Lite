package com.patika.getir_lite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.util.BigDecimalConverter

@Database(entities = [ProductEntity::class, OrderEntity::class], version = 1, exportSchema = false)
@TypeConverters(BigDecimalConverter::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDto(): ProductDao
}
