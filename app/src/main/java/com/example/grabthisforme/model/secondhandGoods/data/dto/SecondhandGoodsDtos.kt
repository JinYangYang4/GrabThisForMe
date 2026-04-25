package com.example.grabthisforme.model.secondhandGoods.data.dto

import com.example.grabthisforme.model.goods.data.dto.GoodsDto
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandTradeInfo

data class SecondhandTradeDto(
    val saleUserId: Long? = null,
    val saleUserName: String = "",
    val saleUserAvatar: String = "",
    val originalPrice: Double = 0.0,
    val quality: String = "",
    val usedTime: String? = null,
    val tradeStatus: Int = SecondhandTradeInfo.STATUS_ON_SALE,
    val negotiable: Boolean = true
)

data class SecondhandGoodsDto(
    val goods: GoodsDto,
    val trade: SecondhandTradeDto = SecondhandTradeDto()
)

