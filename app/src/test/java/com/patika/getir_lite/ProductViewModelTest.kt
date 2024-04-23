package com.patika.getir_lite

import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.data.local.model.toProductWithCount
import com.patika.getir_lite.fake.FakeProductDataSource
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val repository: ProductRepository = FakeProductDataSource()
    private lateinit var viewModel: ProductViewModel

    @Before
    fun setup() {
        viewModel = ProductViewModel(repository)
    }

    @Test
    fun `handle rapid consecutive product events correctly`() = runTest {
        val addCount = 100
        val minusCount = 49
        repeat(addCount) {
            viewModel.onEvent(ProductEvent.OnPlusClick(0L, it))
            advanceTimeBy(200) // mock the real delay
        }

        repeat(minusCount) {
            viewModel.onEvent(ProductEvent.OnMinusClick(0L, it))
            advanceTimeBy(200)
        }

        val expectedCount = addCount - minusCount
        val productResponse = viewModel.products.first()
        if (productResponse is BaseResponse.Success) {
            val pro = productResponse.data.find { it.productId == 0L }
            assertEquals(expectedCount, pro?.count)
        }
    }

    @Test
    fun `handle high load of mixed operations gracefully`() = runTest {
        val products = repository.getProductsAsFlow().first()
        val suggestedProducts = repository.getSuggestedProductsAsFlow().first()

        products.forEach {
            repeat(100) { _ ->
                viewModel.onEvent(ProductEvent.OnPlusClick(it.productId, 1))
                advanceTimeBy(200)
                viewModel.onEvent(ProductEvent.OnMinusClick(it.productId, 1))
                advanceTimeBy(200)
            }
        }

        suggestedProducts.forEach {
            repeat(100) { _ ->
                viewModel.onEvent(ProductEvent.OnPlusClick(it.productId, 1))
                advanceTimeBy(200)
                viewModel.onEvent(ProductEvent.OnMinusClick(it.productId, 1))
                advanceTimeBy(200)
            }
        }

        advanceUntilIdle()

        val productResponse = viewModel.products.first()
        val suggestedProductResponse = viewModel.suggestedProducts.first()
        if (productResponse is BaseResponse.Success && suggestedProductResponse is BaseResponse.Success) {
            val pro = productResponse.data.find { it.productId == 0L }
            val suggestedPro = suggestedProductResponse.data.find { it.productId == 1L }
            assertEquals(0, pro?.count)
            assertEquals(0, suggestedPro?.count)
        }
    }

    @Test
    fun `update in product count reflects in basket with products`() = runTest {
        val product = repository.getProductsAsFlow().first().first()
        viewModel.onEvent(ProductEvent.OnPlusClick(product.productId))
        advanceTimeBy(200)
        advanceUntilIdle()
        val basketResult = viewModel.basketWithProducts.first()
        if (basketResult is BaseResponse.Success) {
            val basket = basketResult.data
            val item = basket.itemsWithProducts.map { it.toProductWithCount() }
                .find { it.productId == product.productId }
            assertEquals(1, item?.count)
        }
    }
}
