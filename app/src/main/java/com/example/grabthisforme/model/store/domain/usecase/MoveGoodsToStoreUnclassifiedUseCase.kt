package com.example.grabthisforme.model.store.domain.usecase

import com.example.grabthisforme.model.store.data.repository.StoreRepository
import javax.inject.Inject

class MoveGoodsToStoreUnclassifiedUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(
        storeId: Long,
        goodsId: Long
    ) {
        storeRepository.moveGoodsToUnclassified(
            storeId = storeId,
            goodsId = goodsId
        )
    }
}
