package com.patika.getir_lite.data.remote.util

sealed class ApiException(message: String): Exception(message) {
    class ProductEmptyException: ApiException("Product list is empty")
    class SuggestedProductEmptyException: ApiException("Suggested product list is empty")
    class BodyNullException: ApiException("Body is null")
}
