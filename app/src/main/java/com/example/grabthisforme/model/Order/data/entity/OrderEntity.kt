package com.example.grabthisforme.model.order.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_cache",
    indices = [
        Index(value = ["buyerId"]),
        Index(value = ["senderId"]),
        Index(value = ["goodsId"]),
        Index(value = ["startTime"]),
        Index(value = ["endTime"])
    ]
)
data class OrderEntity(
    @PrimaryKey val orderId: String,
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
    val isAccepted: Boolean = false
)
