package com.example.grabthisforme.model.order.data.network.dto

data class OrderDto(
    val orderId: String,
    val senderId: Long? = null,
    val senderName: String = "",
    val senderAvatarUrl: String = "",
    val buyerId: Long = 0L,
    val buyerName: String = "",
    val buyerAvatarUrl: String = "",
    val goodsId: Long = 0L,
    val goodsName: String = "",
    val goodsMessage: String = "",
    val goodsPrice: Double = 0.0,
    val goodsPic: String = "",
    val shelfNumber: String = "",
    val aimPosition: String = "",
    val atPosition: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val orderStatus: Int = 0,
    val isAccepted: Boolean = false,
    val orderType: String = "ERRAND",
    val purchaseId: String? = null,
    val storeId: Long = 0L,
    val storeName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val subtotalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val userCouponId: String? = null
)

data class PurchaseRecordDto(
    val recordId: String,
    val purchaseId: String,
    val buyerId: Long,
    val buyerName: String = "",
    val buyerAvatarUrl: String = "",
    val storeId: Long,
    val storeName: String = "",
    val goodsId: Long,
    val goodsName: String = "",
    val goodsMessage: String = "",
    val goodsPic: String = "",
    val quantity: Int,
    val unitPrice: Double,
    val subtotalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val userCouponId: String? = null,
    val createdTime: Long,
    val status: String = "PAID"
)

data class PurchaseResultDto(
    val purchaseId: String,
    val subtotalAmount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double,
    val userCouponId: String? = null,
    val createdTime: Long,
    val records: List<PurchaseRecordDto> = emptyList()
)
