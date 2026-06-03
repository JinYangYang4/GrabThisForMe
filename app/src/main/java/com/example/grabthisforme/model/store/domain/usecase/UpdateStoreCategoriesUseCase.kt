package com.example.grabthisforme.model.store.domain.usecase

import com.example.grabthisforme.model.store.data.repository.StoreRepository
import javax.inject.Inject

class UpdateStoreCategoriesUseCase @Inject constructor(
    private val storeRepository: StoreRepository
) {
    suspend operator fun invoke(
        storeId: Long,
        categories: List<String>,
        renamedCategories: Map<String, String> = emptyMap()
    ) {
        storeRepository.updateStoreCategoriesOnly(
            storeId = storeId,
            categories = categories,
            renamedCategories = renamedCategories
        )
    }
}
