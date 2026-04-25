package com.example.grabthisforme.model.store

import com.example.grabthisforme.model.goods.domain.Goods
import java.math.BigDecimal
import java.util.UUID

data class Store(
    val identity: StoreIdentity,      // 商店核心身份信息（ID、名称、类型）
    val location: StoreLocation,       // 商店位置信息（地址、经纬度）
    val commercialInfo: StoreCommercialInfo, // 商店营业信息（电话、评分、配送费等）
    val statistics: StoreStatistics,   // 商店统计数据（销量）
    val goodsAll: List<Goods>? = null // 该商店的所有商品列表
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

    // 简化构造方法（方便外部直接创建店铺对象，不用手动组装子模型）
    constructor(
        name: String,                          // 店铺名称
        type: String,                          // 店铺类型（快餐/超市/奶茶等）
        address: String,                       // 店铺详细地址
        id: Long = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,  // 店铺唯一ID（自动生成）
        phone: String? = null,                  // 联系电话
        businessHours: String? = null,          // 营业时间
        latitude: Double? = null,               // 纬度
        longitude: Double? = null,              // 经度
        minOrderAmount: BigDecimal = BigDecimal.ZERO,  // 起送价（默认0）
        deliveryFee: BigDecimal = BigDecimal.ZERO,     // 配送费（默认0）
        isOpen: Boolean = true,                 // 是否营业中（默认营业）
        pic: String? = null,                    // 店铺封面/头像图片
        rating: Float = 0.0f,                   // 店铺评分（默认0分）
        tags: List<String> = emptyList(),        // 店铺标签（默认空列表）
        goodsAll: List<Goods>? = null,          // 商品列表（默认null）
        salesVolume: Long = 0                   // 销量（默认0）
    ): this(
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

