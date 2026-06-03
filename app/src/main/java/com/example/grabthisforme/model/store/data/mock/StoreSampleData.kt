package com.example.grabthisforme.model.store.data.mock

import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.domain.StoreCommercialInfo
import com.example.grabthisforme.model.store.domain.StoreIdentity
import com.example.grabthisforme.model.store.domain.StoreLocation
import com.example.grabthisforme.model.store.domain.StoreStatistics

internal object StoreSampleData {
    private const val DEFAULT_BASE_STORE_ID = 101L

    fun createVirtualStores(templateStore: Store? = null): List<Store> {
        val baseStore = templateStore ?: Store(
            identity = StoreIdentity(
                id = DEFAULT_BASE_STORE_ID,
                name = "默认店铺",
                type = "便利店"
            ),
            location = StoreLocation(
                address = "虚拟路 100 号"
            ),
            commercialInfo = StoreCommercialInfo(),
            statistics = StoreStatistics()
        )

        return List(15) { index ->
            baseStore.copy(
                identity = baseStore.identity.copy(
                    id = baseStore.id + index,
                    name = "${baseStore.name}${index + 1}"
                )
            )
        }
    }
}
