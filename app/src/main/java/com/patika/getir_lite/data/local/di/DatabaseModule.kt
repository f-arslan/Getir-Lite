package com.patika.getir_lite.data.local.di

import android.content.Context
import androidx.room.Room
import com.patika.getir_lite.data.local.ProductDao
import com.patika.getir_lite.data.local.ProductDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private var instance: ProductDatabase? = null

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): ProductDatabase {
        if (instance == null) {
            instance = Room.databaseBuilder(context, ProductDatabase::class.java, DB_NAME).build()
        }
        return instance!!
    }

    @Singleton
    @Provides
    fun provideProductDao(db: ProductDatabase): ProductDao = db.productDto()

    fun resetDatabase(@ApplicationContext context: Context) {
        instance?.close()
        context.deleteDatabase(DB_NAME)
        instance = null
        provideDatabase(context)
    }

    private const val DB_NAME = "product"
}
