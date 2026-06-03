package com.example.grabthisforme.model.store.domain.usecase

import com.example.grabthisforme.model.store.data.repository.StoreRepository
import javax.inject.Inject

class AssignGoodsToStoreCategoryUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(
        storeId: Long,
        goodsId: Long,
        category: String
    ) {
        storeRepository.assignGoodsToCategory(
            storeId = storeId,
            goodsId = goodsId,
            category = category
        )
    }
}
