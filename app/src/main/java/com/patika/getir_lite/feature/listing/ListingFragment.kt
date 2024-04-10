package com.patika.getir_lite.feature.listing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.patika.getir_lite.databinding.FragmentListingBinding
import com.patika.getir_lite.feature.BaseFragment
import com.patika.getir_lite.feature.listing.adapter.SuggestedProductAdapter
import com.patika.getir_lite.model.Response
import com.patika.getir_lite.util.MarginItemDecoration
import com.patika.getir_lite.util.ext.scopeWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ListingFragment : BaseFragment<FragmentListingBinding>() {

    private val viewModel: ListingViewModel by viewModels()
    private lateinit var suggestedAdapter: SuggestedProductAdapter

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentListingBinding =
        FragmentListingBinding.inflate(inflater, container, false)

    override fun FragmentListingBinding.initializeViews() {
        viewModel.fetchProductData()

        setupAdapter()
        observeRemoteChanges()

    }

    private fun setupAdapter() {
        suggestedAdapter = SuggestedProductAdapter()
        binding.rvSuggested.adapter = suggestedAdapter
        binding.rvSuggested.addItemDecoration(MarginItemDecoration())
    }

    private fun observeRemoteChanges() {
        scopeWithLifecycle {
            viewModel.remoteUiState.map { it.product }.distinctUntilChanged().collectLatest {
            }
        }
        scopeWithLifecycle {
            viewModel.remoteUiState.map { it.suggestedProduct }.distinctUntilChanged()
                .collectLatest { response ->
                    when (response) {
                        is Response.Error -> {}
                        Response.Loading -> {}
                        is Response.Success -> suggestedAdapter.saveData(response.data.also(::println))
                    }
                }
        }
    }
}
