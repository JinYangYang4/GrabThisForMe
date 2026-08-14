package com.example.grabthisforme.model.coupon.data.network.dto

data class CouponTemplateDto(
    val templateId: Long,
    val title: String,
    val description: String = "",
    val discountAmount: Double,
    val minimumAmount: Double,
    val purchasePrice: Double,
    val validDays: Int,
    val storeId: Long? = null,
    val stock: Int,
    val perUserLimit: Int,
    val purchasedCount: Long,
    val canPurchase: Boolean
)

data class UserCouponDto(
    val userCouponId: String,
    val templateId: Long,
    val title: String,
    val description: String = "",
    val discountAmount: Double,
    val minimumAmount: Double,
    val storeId: Long? = null,
    val purchasePricePaid: Double,
    val acquiredAt: Long,
    val validFrom: Long,
    val validUntil: Long,
    val status: String,
    val usedPurchaseId: String? = null,
    val applicable: Boolean = false
)

data class CouponPurchaseDto(
    val purchaseStatus: String,
    val paidAmount: Double,
    val coupon: UserCouponDto
)
