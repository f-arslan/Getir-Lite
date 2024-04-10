package com.patika.getir_lite.model

sealed class Response<out T> {
    data object Loading : Response<Nothing>()
    data class Success<T>(val data: T) : Response<T>()
    data class Error(val exception: Exception) : Response<Nothing>()
}