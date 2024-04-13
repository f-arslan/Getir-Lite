package com.patika.getir_lite.data.remote.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.patika.getir_lite.BuildConfig
import com.patika.getir_lite.data.di.AppDispatchers
import com.patika.getir_lite.data.di.Dispatcher
import com.patika.getir_lite.data.remote.RemoteDataSource
import com.patika.getir_lite.data.remote.RemoteRepository
import com.patika.getir_lite.data.remote.api.ProductApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logging = HttpLoggingInterceptor()
        logging.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
        else HttpLoggingInterceptor.Level.NONE
        return logging
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
            .build()

    private val json = Json { ignoreUnknownKeys = true }

    @Singleton
    @Provides
    fun provideProductApi(okHttpClient: OkHttpClient): ProductApi =
        Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(BASE_URL)
            .build()
            .create(ProductApi::class.java)

    @Singleton
    @Provides
    fun provideProductRepository(
        productApi: ProductApi,
        @Dispatcher(AppDispatchers.IO) dispatcher: CoroutineDispatcher
    ): RemoteRepository = RemoteDataSource(productApi, dispatcher)
    private const val BASE_URL =
        "https://65c38b5339055e7482c12050.mockapi.io/api/"
}
