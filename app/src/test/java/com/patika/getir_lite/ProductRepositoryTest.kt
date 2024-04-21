package com.patika.getir_lite

import com.patika.getir_lite.data.local.model.OrderStatus
import com.patika.getir_lite.fake.FakeProductDataSource
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.ProductType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryTest {
    private lateinit var repository: FakeProductDataSource

    @Before
    fun setUp() {
        repository = FakeProductDataSource()
    }

    @Test
    fun `concurrent updates to item count result in correct final count`() = runTest {
        val productId = repository.getProductsAsFlow().first().first().productId
        val initialProductCount =
            repository.getProductsAsFlow().first().find { it.productId == productId }?.count ?: 0
        val incrementCount = 100

        val jobs = List(incrementCount) {
            launch {
                repository.updateItemCount(productId, CountType.PLUS_ONE)
            }
        }

        jobs.forEach { it.join() }

        val finalProductCount =
            repository.getProductsAsFlow().first().find { it.productId == productId }?.count
        assertEquals(
            "Final count should be initial plus increment count",
            initialProductCount + incrementCount,
            finalProductCount
        )
    }


    @Test
    fun `concurrent updates to different products are isolated`() = runTest {
        val products = repository.getProductsAsFlow().first()
        val productId1 = products[0].productId
        val productId2 = products[1].productId
        val incrementCount = 50

        val jobs = List(incrementCount) {
            launch { repository.updateItemCount(productId1, CountType.PLUS_ONE) }
            launch { repository.updateItemCount(productId2, CountType.PLUS_ONE) }
        }

        jobs.forEach { it.join() }

        val finalCount1 =
            repository.getProductsAsFlow().first().find { it.productId == productId1 }?.count
        val finalCount2 =
            repository.getProductsAsFlow().first().find { it.productId == productId2 }?.count
        assertTrue(
            "Both products should have counts increased independently and correctly",
            finalCount1 == incrementCount && finalCount2 == incrementCount
        )
    }


    @Test
    fun `concurrent updates do not cause data inconsistency`() = runTest {
        val productId = repository.getProductsAsFlow().first().first().productId

        launch { repository.updateItemCount(productId, CountType.PLUS_ONE) }
        launch { repository.updateItemCount(productId, CountType.PLUS_ONE) }
        launch { repository.updateItemCount(productId, CountType.MINUS_ONE) }

        advanceUntilIdle()

        val expectedCount = 1
        val productCount =
            repository.getProductsAsFlow().first().find { it.productId == productId }?.count
        assertEquals(
            "Product count should be correct after concurrent updates",
            expectedCount,
            productCount,
        )
    }

    @Test
    fun `getProductsAsFlow emits only products`() = runTest {
        val products = repository.getProductsAsFlow().first()
        assertTrue(products.isNotEmpty())
        products.forEach {
            assertTrue(it.productType == ProductType.PRODUCT)
        }
    }

    @Test
    fun `concurrent basket creation results in a single basket`() = runTest {
        repository.clearBasket()

        val productId = repository.getProductsAsFlow().first().first().productId
        val creationAttempts = 10

        val jobs = List(creationAttempts) {
            launch {
                repository.updateItemCount(productId, CountType.PLUS_ONE)
            }
        }

        jobs.forEach { it.join() }

        val baskets = repository.orders.filter { it.orderStatus == OrderStatus.ON_BASKET }
        assertEquals("There should only be one basket created", 1, baskets.size)
    }

    @Test
    fun `updateItemCount increases item count correctly`() = runTest {
        val initialProducts = repository.getProductsAsFlow().first()
        val productId = initialProducts.first().productId
        repository.updateItemCount(productId, CountType.PLUS_ONE)
        val updatedProducts = repository.getProductsAsFlow().first()
        val updatedProduct = updatedProducts.find { it.productId == productId }

        assertEquals("Product count should be increased by 1", updatedProduct?.count, 1)
    }

    @Test
    fun `initial state is correct`() = runTest {
        val products = repository.getProductsAsFlow().first()
        val basket = repository.getBasketWithProductsAsFlow().first()
        assertTrue("Initial products should be correctly loaded", products.isNotEmpty())
        assertNull("Initial basket should be empty", basket)
    }

    @Test
    fun `clearBasket clears the basket successfully`() = runTest {
        val initialProducts = repository.getProductsAsFlow().first()
        val productId1 = initialProducts.first().productId
        val productId2 = initialProducts[1].productId
        repository.updateItemCount(productId1, CountType.PLUS_ONE)
        repository.updateItemCount(productId2, CountType.PLUS_ONE)

        repository.updateItemCount(productId1, CountType.MINUS_ONE)


        repository.clearBasket()

        val basket = repository.getBasketWithProductsAsFlow().first()
        assertTrue(
            "Basket should be empty after clearing",
            basket == null
        )
    }


    @Test
    fun `updateItemCount decreases item count correctly`() = runTest {
        val initialProducts = repository.getProductsAsFlow().first()
        val productId1 = initialProducts.first().productId
        val productId2 = initialProducts[1].productId
        repository.updateItemCount(productId1, CountType.PLUS_ONE)
        repository.updateItemCount(productId2, CountType.PLUS_ONE)
        repository.updateItemCount(productId1, CountType.PLUS_ONE)
        repository.updateItemCount(productId2, CountType.PLUS_ONE)
        repository.updateItemCount(productId1, CountType.PLUS_ONE)
        repository.updateItemCount(productId1, CountType.MINUS_ONE)

        val basket = repository.getBasketWithProductsAsFlow().first()

        val product1 = basket?.itemsWithProducts?.find { it.item.productId == productId1 }
        val product2 = basket?.itemsWithProducts?.find { it.item.productId == productId2 }
        assertTrue(product1?.item?.count == 2)
        assertTrue(product2?.item?.count == 2)
    }
}
