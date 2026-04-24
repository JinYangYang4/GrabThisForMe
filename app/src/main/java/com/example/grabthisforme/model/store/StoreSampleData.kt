package com.example.grabthisforme.model.store

internal object StoreSampleData {
    fun createVirtualStores(templateStore: Store? = null): List<Store> {
        val baseStore = templateStore ?: Store(
            name = "默认虚拟商店",
            type = "便利店",
            address = "XX市虚拟路100号"
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
