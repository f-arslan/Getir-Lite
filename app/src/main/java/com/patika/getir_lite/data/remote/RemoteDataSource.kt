package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.di.AppDispatchers.IO
import com.patika.getir_lite.data.di.Dispatcher
import com.patika.getir_lite.data.remote.api.ProductApi
import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.data.remote.util.ApiException.BodyNullException
import com.patika.getir_lite.data.remote.util.ApiException.ProductEmptyException
import com.patika.getir_lite.data.remote.util.ApiException.SuggestedProductEmptyException
import com.patika.getir_lite.data.remote.model.DataResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val productApi: ProductApi,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : RemoteRepository {
    override suspend fun getProductDtos(): DataResult<List<ProductDto>> =
        remoteResultWrapper(productApi::getProducts) { responseBody ->
            if (responseBody.isNotEmpty()) {
                DataResult.Success(responseBody.first().productDtos ?: emptyList())
            } else {
                DataResult.Error(ProductEmptyException())
            }
        }

    override suspend fun getSuggestedProductDtos(): DataResult<List<SuggestedProductDto>> =
        remoteResultWrapper(productApi::getSuggestedProducts) { responseBody ->
            if (responseBody.isNotEmpty()) {
                DataResult.Success(responseBody.first().suggestedProductDtos)
            } else {
                DataResult.Error(SuggestedProductEmptyException())
            }
        }

    private suspend fun <T, R> remoteResultWrapper(
        apiCall: suspend () -> Response<T>,
        transformSuccess: (T) -> DataResult<R>
    ): DataResult<R> {
        return withContext(ioDispatcher) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    response.body()?.let(transformSuccess)
                        ?: DataResult.Error(BodyNullException())
                } else {
                    DataResult.Error(HttpException(response))
                }
            } catch (e: Exception) {
                DataResult.Error(e)
            }
        }
    }
}
