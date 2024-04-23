package com.patika.getir_lite

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.data.local.model.BasketWithProducts
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.TopLevelException.GenericException
import com.patika.getir_lite.util.TopLevelException.NoConnectionException
import com.patika.getir_lite.util.TopLevelException.ProductNotLoadedException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min
import kotlin.math.pow

/**
 * ViewModel responsible for managing UI-related data in a lifecycle-conscious way.
 * It interacts with [ProductRepository] to fetch and manage product data, handling both
 * ordinary product queries and specialized cases such as suggested products and basket operations.
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
) :
    ViewModel() {

    /**
     * A [StateFlow] of [BaseResponse] that provides a stream of [List] of [ProductWithCount].
     * It manages the fetching and updating of product data, handling loading states, success,
     * errors due to product not being loaded, and connectivity issues.
     *
     * @property products The flow of product data encapsulated in [BaseResponse], which can
     * either be a successful data load ([BaseResponse.Success]), an error ([BaseResponse.Error]),
     * or a loading state ([BaseResponse.Loading]).
     */
    val products: StateFlow<BaseResponse<List<ProductWithCount>>> = productRepository
        .getProductsAsFlow()
        .transform {
            val state = productRepository.getStatus().firstOrNull()
            when {
                state == null -> emit(BaseResponse.Loading)
                !state.isProductLoaded -> throw ProductNotLoadedException()
                state.isProductLoaded -> emit(BaseResponse.Success(it))
                else -> emit(BaseResponse.Loading)
            }
        }
        .retryWhen { cause, attempt ->
            if (cause is ProductNotLoadedException && attempt < MAX_RETRIES) {
                val delayTime = calculateDelay(attempt)
                if (attempt > 0) emit(BaseResponse.Error(NoConnectionException(delayTime)))
                productRepository.fetchDataFromRemote()
                delay(delayTime)
                true
            } else {
                false
            }
        }
        .catch { error ->
            if (error !is ProductNotLoadedException) {
                emit(BaseResponse.Error(GenericException(error.message)))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    val suggestedProducts = productRepository
        .getSuggestedProductsAsFlow()
        .transform {
            val state = productRepository.getStatus().firstOrNull()
            when {
                state == null -> emit(BaseResponse.Loading)
                !state.isSuggestedProductLoaded -> throw ProductNotLoadedException()
                state.isSuggestedProductLoaded -> emit(BaseResponse.Success(it))
                else -> emit(BaseResponse.Loading)
            }
        }
        .retryWhen { cause, attempt ->
            if (cause is ProductNotLoadedException && attempt < MAX_RETRIES) {
                if (attempt > 0) productRepository.fetchDataFromRemote()
                delay(calculateDelay(attempt))
                true
            } else {
                false
            }
        }
        .catch { error ->
            if (error !is ProductNotLoadedException) {
                emit(BaseResponse.Error(GenericException(error.message)))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    val basket: Flow<BaseResponse<Order?>> = productRepository
        .getBasketAsFlow()
        .map<Order?, BaseResponse<Order?>> { BaseResponse.Success(it) }
        .catch { cause ->
            emit(BaseResponse.Error(GenericException(cause.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    val basketWithProducts: Flow<BaseResponse<BasketWithProducts>> = productRepository
        .getBasketWithProductsAsFlow()
        .transform {
            when (it) {
                null -> emit(BaseResponse.Loading)
                else -> emit(BaseResponse.Success(it))
            }
        }
        .catch { error ->
            emit(BaseResponse.Error(GenericException(error.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    @MainThread
    fun initializeProductData() = viewModelScope.launch {
        productRepository.fetchDataFromRemote()
    }


    private var job: Job? = null

    /**
     * Handles incoming events related to product interactions, such as add or remove actions.
     *
     * @param event The product event to handle.
     */
    fun onEvent(event: ProductEvent) {
        job?.cancel()
        job = viewModelScope.launch {
            when (event) {
                is ProductEvent.OnDeleteClick -> {
                    productRepository.updateItemCount(event.entityId, CountType.MINUS_ONE)
                }

                is ProductEvent.OnMinusClick -> {
                    productRepository.updateItemCount(event.entityId, CountType.MINUS_ONE)
                }

                is ProductEvent.OnPlusClick -> {
                    productRepository.updateItemCount(event.entityId, CountType.PLUS_ONE)
                }
            }
        }
    }

    companion object {
        private const val MAX_RETRIES = 50
        private const val RETRY_DELAY_MS = 2000L
        private const val MAX_DELAY_MS = 30000L
        val calculateDelay: (Long) -> Long =
            { attempt: Long ->
                min(
                    MAX_DELAY_MS,
                    RETRY_DELAY_MS * (2.0.pow(attempt.toDouble())).toLong()
                )
            }
    }
}
