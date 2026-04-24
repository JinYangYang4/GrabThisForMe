package com.example.grabthisforme.model.store

import java.math.BigDecimal

/**
 * 店铺 基础标识信息
 */
data class StoreIdentity(
    val id: Long,        // 店铺唯一ID
    val name: String,    // 店铺名称
    val type: String     // 店铺类型（如：快餐、奶茶、超市、水果店）
)
/**
 * 店铺 位置信息
 */
data class StoreLocation(
    val address: String,          // 详细地址
    val latitude: Double? = null, // 纬度
    val longitude: Double? = null // 经度
)

/**
 * 商店商业/营业信息
 */
data class StoreCommercialInfo(
    val phone: String? = null,            // 商家联系电话
    val businessHours: String? = null,    // 营业时间（例如：09:00-21:00）
    val minOrderAmount: BigDecimal = BigDecimal.ZERO, // 最低起送金额 / 起送价
    val deliveryFee: BigDecimal = BigDecimal.ZERO,    // 配送费
    val isOpen: Boolean = true,           // 是否营业中（true=营业，false=休息）
    val pic: String? = null,              // 店铺封面图片 / 商家头像URL
    val rating: Float = 0.0f,            // 店铺评分（0-5分）
    val tags: List<String> = emptyList()  // 店铺标签（例如：新店、免配送费、24小时营业）
)

//销量
data class StoreStatistics(
    val salesVolume: Long = 0
)

