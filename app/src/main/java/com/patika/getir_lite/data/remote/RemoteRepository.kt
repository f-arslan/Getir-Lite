package com.patika.getir_lite.data.remote

import com.patika.getir_lite.data.remote.model.ProductDto
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.model.BaseResponse

/**
 * Interface defining the methods for fetching data from a remote server. Implementations should handle
 * network interactions to retrieve product and suggested product data, returning it in a standardized response format.
 */
interface RemoteRepository {

    /**
     * Fetches a list of products from the remote server.
     *
     * @return A [BaseResponse] containing a list of [ProductDto] if the fetch is successful, encapsulating the data or error state.
     */
    suspend fun getProductDtos(): BaseResponse<List<ProductDto>>

    /**
     * Fetches a list of suggested products from the remote server.
     *
     * @return A [BaseResponse] containing a list of [SuggestedProductDto] if the fetch is successful, encapsulating the data or error state.
     */
    suspend fun getSuggestedProductDtos(): BaseResponse<List<SuggestedProductDto>>
}
