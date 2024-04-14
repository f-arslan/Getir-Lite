package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.di.AppDispatchers.IO
import com.patika.getir_lite.data.di.Dispatcher
import com.patika.getir_lite.data.remote.api.ProductApi
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.util.TopLevelException.BodyNullException
import com.patika.getir_lite.util.TopLevelException.HttpException
import com.patika.getir_lite.util.TopLevelException.GenericException
import com.patika.getir_lite.util.TopLevelException.ProductEmptyException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val productApi: ProductApi,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : RemoteRepository {
    override suspend fun getProductDtos(): BaseResponse<List<ProductDto>> =
        remoteResultWrapper(productApi::getProducts) { responseBody ->
            if (responseBody.isNotEmpty()) {
                BaseResponse.Success(responseBody.first().productDtos ?: emptyList())
            } else {
                BaseResponse.Error(ProductEmptyException())
            }
        }

    override suspend fun getSuggestedProductDtos(): BaseResponse<List<SuggestedProductDto>> =
        remoteResultWrapper(productApi::getSuggestedProducts) { responseBody ->
            if (responseBody.isNotEmpty()) {
                BaseResponse.Success(responseBody.first().suggestedProductDtos)
            } else {
                BaseResponse.Error(ProductEmptyException())
            }
        }

    private suspend fun <T, R> remoteResultWrapper(
        apiCall: suspend () -> Response<T>,
        transformSuccess: (T) -> BaseResponse<R>
    ): BaseResponse<R> {
        return withContext(ioDispatcher) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    response.body()?.let(transformSuccess)
                        ?: BaseResponse.Error(BodyNullException())
                } else {
                    BaseResponse.Error(HttpException(response.code()))
                }
            } catch (e: Exception) {
                BaseResponse.Error(GenericException(e.message))
            }
        }
    }
}
