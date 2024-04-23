package com.patika.getir_lite.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.patika.getir_lite.data.local.model.ItemEntity
import com.patika.getir_lite.data.local.model.ProductEntity
import com.patika.getir_lite.data.local.model.OrderEntity
import com.patika.getir_lite.data.local.model.StatusEntity
import com.patika.getir_lite.util.BigDecimalConverter

/**
 * This class defines the Room database setup including the entities involved and the database version.
 * It also specifies a type converter to handle specific data types that Room cannot handle natively.
 *
 * @property productDto Provides access to [ProductDao], which contains methods for accessing and manipulating product-related data stored in the database.
 */
@Database(
    entities = [OrderEntity::class, ProductEntity::class, ItemEntity::class, StatusEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class ProductDatabase : RoomDatabase() {
    abstract fun productDto(): ProductDao
}
