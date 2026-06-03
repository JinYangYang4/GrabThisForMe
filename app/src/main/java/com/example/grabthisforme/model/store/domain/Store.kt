package com.example.grabthisforme.model.store.domain

import com.example.grabthisforme.model.store.data.mock.StoreSampleData
import java.math.BigDecimal

data class Store(
    val identity: StoreIdentity,
    val location: StoreLocation,
    val commercialInfo: StoreCommercialInfo,
    val statistics: StoreStatistics
) {
    val id: Long get() = identity.id
    val ownerId: Long get() = identity.ownerId
    val name: String get() = identity.name
    val type: String get() = identity.type
    val address: String get() = location.address
    val phone: String? get() = commercialInfo.phone
    val businessHours: String? get() = commercialInfo.businessHours
    val latitude: Double? get() = location.latitude
    val longitude: Double? get() = location.longitude
    val minOrderAmount: BigDecimal get() = commercialInfo.minOrderAmount
    val deliveryFee: BigDecimal get() = commercialInfo.deliveryFee
    val isOpen: Boolean get() = commercialInfo.isOpen
    val pic: String? get() = commercialInfo.pic
    val rating: Float get() = commercialInfo.rating
    val tags: List<String> get() = commercialInfo.tags
    val salesVolume: Long get() = statistics.salesVolume

    fun getLocationInfo(): String {
        return if (latitude != null && longitude != null) {
            "[$latitude, $longitude] $address"
        } else {
            address
        }
    }

    companion object {
        const val CATEGORY_ALL = "全部"
        const val CATEGORY_UNCLASSIFIED = "未分类"

        fun createVirtualStores(templateStore: Store? = null): List<Store> =
            StoreSampleData.createVirtualStores(templateStore)
    }
}
