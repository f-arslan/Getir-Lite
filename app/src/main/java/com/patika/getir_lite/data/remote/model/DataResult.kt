package com.patika.getir_lite.data.remote.model

import com.patika.getir_lite.model.Response

sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val exception: Exception) : DataResult<Nothing>()

    fun toResponse(): Response<T> = when (this) {
        is Success -> Response.Success(data)
        is Error -> Response.Error(exception)
    }
}
