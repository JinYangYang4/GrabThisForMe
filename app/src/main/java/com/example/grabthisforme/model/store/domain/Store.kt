package com.example.grabthisforme.model.store.domain

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.mock.StoreSampleData
import java.math.BigDecimal
import java.util.UUID

data class Store(
    val identity: StoreIdentity,
    val location: StoreLocation,
    val commercialInfo: StoreCommercialInfo,
    val statistics: StoreStatistics,
    val goodsAll: List<Goods>? = null
) {
    val id: Long get() = identity.id
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

    constructor(
        name: String,
        type: String,
        address: String,
        id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
        phone: String? = null,
        businessHours: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        minOrderAmount: BigDecimal = BigDecimal.ZERO,
        deliveryFee: BigDecimal = BigDecimal.ZERO,
        isOpen: Boolean = true,
        pic: String? = null,
        rating: Float = 0.0f,
        tags: List<String> = emptyList(),
        goodsAll: List<Goods>? = null,
        salesVolume: Long = 0
    ) : this(
        identity = StoreIdentity(
            id = id,
            name = name,
            type = type
        ),
        location = StoreLocation(
            address = address,
            latitude = latitude,
            longitude = longitude
        ),
        commercialInfo = StoreCommercialInfo(
            phone = phone,
            businessHours = businessHours,
            minOrderAmount = minOrderAmount,
            deliveryFee = deliveryFee,
            isOpen = isOpen,
            pic = pic,
            rating = rating,
            tags = tags
        ),
        statistics = StoreStatistics(
            salesVolume = salesVolume
        ),
        goodsAll = goodsAll
    )

    fun getLocationInfo(): String {
        return if (latitude != null && longitude != null) {
            "[$latitude, $longitude] $address"
        } else {
            address
        }
    }

    fun withGoods(goods: List<Goods>?): Store {
        return copy(goodsAll = goods)
    }

    companion object {
        fun createVirtualStores(templateStore: Store? = null): List<Store> =
            StoreSampleData.createVirtualStores(templateStore)
    }
}
