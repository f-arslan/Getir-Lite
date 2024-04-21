package com.patika.getir_lite

import androidx.annotation.MainThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.AnimationState.FINISHED
import com.patika.getir_lite.AnimationState.IDLE
import com.patika.getir_lite.AnimationState.OPENED
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.BaseResponse
import com.patika.getir_lite.model.BasketWithProducts
import com.patika.getir_lite.model.CountType
import com.patika.getir_lite.model.Order
import com.patika.getir_lite.model.ProductEvent
import com.patika.getir_lite.model.ProductWithCount
import com.patika.getir_lite.util.TopLevelException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(private val productRepository: ProductRepository) :
    ViewModel() {

    private val actionCompletionSignal = Channel<AnimationState>(Channel.UNLIMITED)

    val products: StateFlow<BaseResponse<List<ProductWithCount>>> = productRepository
        .getProductsAsFlow()
        .transform {
            when {
                it.isEmpty() -> emit(BaseResponse.Loading)
                else -> emit(BaseResponse.Success(it))
            }
        }
        .catch { error ->
            emit(BaseResponse.Error(TopLevelException.GenericException(error.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    val suggestedProducts = productRepository
        .getSuggestedProductsAsFlow()
        .combineTransform(actionCompletionSignal.consumeAsFlow()) { suggestedProducts, completionSignal ->
            when (completionSignal) {
                FINISHED -> {
                    emit(BaseResponse.Success(suggestedProducts))
                    actionCompletionSignal.send(IDLE)
                }

                OPENED -> {
                    emit(BaseResponse.Success(suggestedProducts))
                }

                else -> emit(BaseResponse.Loading)
            }
        }
        .catch { error ->
            emit(BaseResponse.Error(TopLevelException.GenericException(error.message)))
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
            emit(BaseResponse.Error(TopLevelException.GenericException(cause.message)))
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
            emit(BaseResponse.Error(TopLevelException.GenericException(error.message)))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BaseResponse.Loading
        )

    @MainThread
    fun initializeProductData() = viewModelScope.launch {
        actionCompletionSignal.trySend(OPENED)
        productRepository.syncWithRemote()
    }

    fun onEvent(event: ProductEvent) {
        viewModelScope.launch {
            when (event) {
                is ProductEvent.OnDeleteClick -> {
                    productRepository.updateItemCount(event.entityId, CountType.MINUS_ONE)
                }

                is ProductEvent.OnMinusClick -> {
                    productRepository.updateItemCount(event.entityId, CountType.MINUS_ONE)
                    if (event.count > 1) notifyActionCompleted(OPENED)
                }

                is ProductEvent.OnPlusClick -> {
                    productRepository.updateItemCount(event.entityId, CountType.PLUS_ONE)
                    if (event.count >= 1) notifyActionCompleted(OPENED)
                }
            }
        }
    }

    fun notifyActionCompleted(animationState: AnimationState) {
        viewModelScope.launch {
            actionCompletionSignal.send(animationState)
        }
    }
}

enum class AnimationState {
    IDLE, FINISHED, OPENED
}
