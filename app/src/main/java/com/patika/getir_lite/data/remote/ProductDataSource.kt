package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.di.AppDispatchers.IO
import com.patika.getir_lite.data.di.Dispatcher
import com.patika.getir_lite.data.remote.api.ProductApi
import com.patika.getir_lite.data.remote.api.SuggestedProductApi
import com.patika.getir_lite.data.remote.model.ApiResult
import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.data.remote.util.ApiException.BodyNullException
import com.patika.getir_lite.data.remote.util.ApiException.ProductEmptyException
import com.patika.getir_lite.data.remote.util.ApiException.SuggestedProductEmptyException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class ProductDataSource @Inject constructor(
    private val productApi: ProductApi,
    private val suggestedProductApi: SuggestedProductApi,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher
) : ProductRepository {
    override suspend fun getProducts(): ApiResult<List<ProductDto>> =
        apiResultWrapper({ productApi.getProducts() }) { responseBody ->
            if (responseBody.isNotEmpty()) {
                ApiResult.Success(responseBody.first().productDtos)
            } else {
                ApiResult.Error(ProductEmptyException())
            }
        }

    override suspend fun getSuggestedProducts(): ApiResult<List<SuggestedProductDto>> =
        apiResultWrapper({ suggestedProductApi.getSuggestedProducts() }) { responseBody ->
            if (responseBody.isNotEmpty()) {
                ApiResult.Success(responseBody.first().suggestedProductDtos)
            } else {
                ApiResult.Error(SuggestedProductEmptyException())
            }
        }

    private suspend fun <T, R> apiResultWrapper(
        apiCall: suspend () -> Response<T>,
        transformSuccess: (T) -> ApiResult<R>
    ): ApiResult<R> {
        return withContext(ioDispatcher) {
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    response.body()?.let(transformSuccess)
                        ?: ApiResult.Error(BodyNullException())
                } else {
                    ApiResult.Error(HttpException(response))
                }
            } catch (e: Exception) {
                ApiResult.Error(e)
            }
        }
    }
}
