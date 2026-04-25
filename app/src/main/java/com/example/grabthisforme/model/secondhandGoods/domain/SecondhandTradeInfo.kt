package com.example.grabthisforme.model.secondhandGoods.domain

import com.example.grabthisforme.model.user.User

data class SecondhandTradeInfo(
    val saleUser: User? = null,
    val originalPrice: Double = 0.0,
    val quality: String = "",
    val usedTime: String? = null,
    val tradeStatus: Int = STATUS_ON_SALE,
    val negotiable: Boolean = true
) {
    companion object {
        const val STATUS_ON_SALE = 0
        const val STATUS_RESERVED = 1
        const val STATUS_SOLD = 2
        const val STATUS_OFF_SHELF = 3
    }
}

