package com.patika.getir_lite.util

sealed class TopLevelException(message: String) : Exception(message) {
    class ProductEmptyException : TopLevelException("Product is empty")
    class BodyNullException : TopLevelException("Body is null")
    class GenericException(message: String?) : TopLevelException("Generic exception: $message")
    class HttpException(code: Int) : TopLevelException("HTTP exception: $code")
    class ProductNotLoadedException : TopLevelException("Product not found")
    class GenericOperationFail :
        TopLevelException("We can't handle your operation, please try again.")
    class NoConnectionException(cooldown: Long) :
        TopLevelException("We couldn't fetch your data. Please check your connection. Cooldown: ${cooldown / 1000} seconds")
}
