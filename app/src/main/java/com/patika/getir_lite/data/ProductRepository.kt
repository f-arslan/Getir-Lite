package com.patika.getir_lite.data

import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.data.local.model.BasketWithProducts
import com.patika.getir_lite.data.local.model.StatusEntity
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.ProductType
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.TopLevelException
import kotlinx.coroutines.flow.Flow

/**
 * Defines the interface for handling product and order data operations. Implementations of this interface
 * should manage data fetching, caching, and business logic associated with product management in the application.
 */
interface ProductRepository {

    /**
     * Retrieves a flow of all products filtered by [ProductType.PRODUCT].
     *
     * @return A [Flow] emitting a list of [ProductWithCount] reflecting the current state of available products.
     */
    fun getProductsAsFlow(): Flow<List<ProductWithCount>>

    /**
     * Fetches data from a remote server and synchronizes it with the local database. This method ensures
     * that data fetching and insertion are thread-safe and are executed sequentially by using a mutex.
     *
     * @return [BaseResponse] indicating the success or error state of the operation.
     * @throws TopLevelException.GenericException if there is any issue during the fetching or database operation.
     */
    suspend fun fetchDataFromRemoteAndSync(): BaseResponse<Unit>

    /**
     * Retrieves a flow of suggested products, often used for displaying recommendations or promotions.
     *
     * @return A [Flow] emitting a list of [ProductWithCount] reflecting the current state of suggested products.
     */
    fun getSuggestedProductsAsFlow(): Flow<List<ProductWithCount>>

    /**
     * Retrieves a flow of the current active basket or order, providing continuous updates as the basket's state changes.
     *
     * @return A [Flow] emitting an [Order] representing the active basket, or null if no basket is active.
     */
    fun getBasketAsFlow(): Flow<Order?>

    /**
     * Retrieves a flow of the current basket along with detailed product information contained within it.
     * This is particularly useful for checkout screens where detailed information about each item in the basket is required.
     *
     * @return A [Flow] emitting a [BasketWithProducts] representing the active basket with its associated products, or null if no basket is active.
     */
    fun getBasketWithProductsAsFlow(): Flow<BasketWithProducts?>

    /**
     * Retrieves a flow of a specific product by its ID, providing continuous updates as the product's details or state changes.
     *
     * @param productId The unique identifier of the product to fetch.
     * @return A [Flow] emitting [ProductWithCount] for the specified product, or null if the product does not exist.
     */
    fun getProductAsFlow(productId: Long): Flow<ProductWithCount?>

    /**
     * Updates the item count for a specific product in an active basket, handling either increment or decrement operations.
     * This method is intended to facilitate immediate updates to basket contents based on user actions.
     *
     * @param productId The product ID whose count will be updated.
     * @param countType The type of count update to perform (add one or subtract one).
     * @return A [Result] encapsulating the success or failure of the update operation.
     */
    suspend fun updateItemCount(productId: Long, countType: CountType): Result<Unit>

    /**
     * Clears all items from the active basket, typically invoked when a user cancels the basket or completes an order.
     * This operation may also trigger updates to associated data such as inventory levels or user profiles.
     *
     * @return A [Boolean] indicating whether the basket was successfully cleared.
     */
    suspend fun clearBasket(): Boolean

    /**
     * Get the current status of the database
     */
    suspend fun getStatus(): List<StatusEntity?>
}
