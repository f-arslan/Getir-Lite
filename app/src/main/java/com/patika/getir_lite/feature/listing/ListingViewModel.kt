package com.patika.getir_lite.feature.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patika.getir_lite.data.ProductRepository
import com.patika.getir_lite.model.ProductEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    fun onEvent(event: ProductEvent) {
        viewModelScope.launch {
            when (event) {
                is ProductEvent.OnDeleteClick -> {
                    productRepository.updateItemCount(event.entityId, -1)
                }

                is ProductEvent.OnMinusClick -> {
                    productRepository.updateItemCount(event.entityId, -1)
                }

                is ProductEvent.OnPlusClick -> {
                    productRepository.updateItemCount(event.entityId, 1)
                }
            }
        }
    }
}
