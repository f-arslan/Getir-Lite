package com.patika.getir_lite.data.di

import com.patika.getir_lite.data.ProductDataSource
import com.patika.getir_lite.data.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface DataBinderModule {
    @Binds
    fun bindProductRepository(productDataSource: ProductDataSource): ProductRepository
}
