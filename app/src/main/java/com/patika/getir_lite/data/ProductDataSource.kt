package com.patika.getir_lite.data

import com.patika.getir_lite.data.remote.RemoteRepository
import com.patika.getir_lite.data.remote.model.SuggestedProductDto
import com.patika.getir_lite.data.remote.model.toDomainModel
import com.patika.getir_lite.data.remote.model.DataResult
import com.patika.getir_lite.model.Product
import javax.inject.Inject

class ProductDataSource @Inject constructor(
    private val remoteRepository: RemoteRepository
) : ProductRepository {
    override suspend fun getProducts(): DataResult<List<Product>> {
        return when (val dataResult = remoteRepository.getProductDtos()) {
            is DataResult.Success -> {
                val products = dataResult.data.mapNotNull { dto ->
                    dto.toDomainModel().takeIf { it.attribute != null }
                }

                DataResult.Success(products)
            }

            is DataResult.Error -> dataResult
        }
    }

    override suspend fun getSuggestedProducts(): DataResult<List<Product>> {
        return when (val dataResult = remoteRepository.getSuggestedProductDtos()) {
            is DataResult.Success -> {
                val suggestedProducts = dataResult.data.map(SuggestedProductDto::toDomainModel)
                DataResult.Success(suggestedProducts)
            }

            is DataResult.Error -> dataResult
        }
    }
}
