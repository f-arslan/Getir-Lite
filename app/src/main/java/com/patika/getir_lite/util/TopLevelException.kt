package com.patika.getir_lite.util

sealed class TopLevelException(message: String) : Exception(message) {
    class ProductEmptyException : TopLevelException("Product is empty")
    class BodyNullException : TopLevelException("Body is null")
    class GenericException(message: String?) : TopLevelException("Generic exception: $message")
    class HttpException(code: Int) : TopLevelException("HTTP exception: $code")
    class ProductNotFoundException : TopLevelException("Product not found")
    class CurrentOrderNotFound : TopLevelException("Current order not found")
    class GenericOperationFail: TopLevelException("We can't handle your operation, please try again.")
}
