package com.example.grabthisforme.model.store

import java.math.BigDecimal
import java.util.UUID

data class Store(
    val name: String,
    val type: String,
    val address: String,
    val id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE, // 生成唯一非负Long ID
    val phone: String? = null,
    val businessHours: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val isOpen: Boolean = true,
    val pic: String? = null,
    val rating: Float = 0.0f,
    val tags: List<String> = emptyList()
) {

    fun getLocationInfo(): String {
        return if (latitude != null && longitude != null) {
            "[$latitude, $longitude] $address"
        } else {
            address
        }
    }
    companion object {
        fun createVirtualStores(templateStore: Store? = null): List<Store> {
            val baseStore = templateStore ?: Store(
                name = "默认虚拟店铺",
                type = "便利店",
                address = "XX市虚拟路100号"
            )

            return List(15) { index ->
                baseStore.copy(
                    id = baseStore.id + index + 1,
                    name = "${baseStore.name}${index + 1}"
                )
            }
        }
    }


}