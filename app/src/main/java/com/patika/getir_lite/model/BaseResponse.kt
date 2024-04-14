package com.patika.getir_lite.model

import com.patika.getir_lite.util.TopLevelException

sealed class BaseResponse<out T> {
    data object Loading : BaseResponse<Nothing>()
    data class Success<T>(val data: T) : BaseResponse<T>()
    data class Error(val exception: TopLevelException) : BaseResponse<Nothing>()
}
