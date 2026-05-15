package com.example.grabthisforme.model.store.domain

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.mock.StoreSampleData
import java.math.BigDecimal
import java.util.LinkedHashMap
import java.util.UUID

data class Store(
    val identity: StoreIdentity,
    val location: StoreLocation,
    val commercialInfo: StoreCommercialInfo,
    val statistics: StoreStatistics,
    val goodsGroups: List<StoreGoodsGroup> = emptyList()
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

    val category: List<String>
        get() = goodsGroups.map { it.category }.filter { it.isNotBlank() }.distinct()

    val goodsAll: List<Goods>
        get() = goodsGroups.flatMap { it.goods }

    constructor(
        name: String,
        type: String,
        address: String,
        id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
        ownerId: Long = 0L,
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
        category: List<String> = emptyList(),
        salesVolume: Long = 0
    ) : this(
        identity = StoreIdentity(
            id = id,
            name = name,
            type = type,
            ownerId = ownerId
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
        goodsGroups = composeGoodsGroups(goodsAll,category)
    )

    fun getLocationInfo(): String {
        return if (latitude != null && longitude != null) {
            "[$latitude, $longitude] $address"
        } else {
            address
        }
    }

    fun withGoods(goods: List<Goods>?): Store {
        return copy(goodsGroups = composeGoodsGroups(goods, category))
    }

    fun withCategories(categories: List<String>): Store {
        return copy(goodsGroups = composeGoodsGroups(goodsAll, categories))
    }

    companion object {
        private const val DEFAULT_CATEGORY = "全部"

        fun createVirtualStores(templateStore: Store? = null): List<Store> =
            StoreSampleData.createVirtualStores(templateStore)

        fun composeGoodsGroups(
            goodsAll: List<Goods>?,
            categories: List<String> = emptyList()
        ): List<StoreGoodsGroup> {
            val goodsList = goodsAll.orEmpty()
            val normalizedCategories = categories.map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            if (normalizedCategories.isEmpty()) {
                return if (goodsList.isEmpty()) {
                    emptyList()
                } else {
                    listOf(StoreGoodsGroup(category = DEFAULT_CATEGORY, goods = goodsList))
                }
            }

            val orderedMap = LinkedHashMap<String, MutableList<Goods>>()
            normalizedCategories.forEach { groupCategory ->
                orderedMap[groupCategory] = mutableListOf()
            }
            if (goodsList.isNotEmpty()) {
                orderedMap[normalizedCategories.first()]?.addAll(goodsList)
            }

            return orderedMap.map { (groupCategory, goodsInCategory) ->
                StoreGoodsGroup(
                    category = groupCategory,
                    goods = goodsInCategory.toList()
                )
            }
        }
    }
}
