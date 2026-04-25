package com.example.grabthisforme.model.store.data.mock

import com.example.grabthisforme.model.store.domain.Store

internal object StoreSampleData {
    fun createVirtualStores(templateStore: Store? = null): List<Store> {
        val baseStore = templateStore ?: Store(
            name = "Default Store",
            type = "Convenience",
            address = "Virtual Road 100"
        )

        return List(15) { index ->
            baseStore.copy(
                identity = baseStore.identity.copy(
                    id = baseStore.id + index + 1,
                    name = "${baseStore.name}${index + 1}"
                )
            )
        }
    }
}
