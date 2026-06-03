package com.example.grabthisforme.model.secondhandGoods.mapper

import com.example.grabthisforme.model.goods.data.dto.GoodsDto
import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDto
import com.example.grabthisforme.model.secondhandGoods.data.dto.SecondhandGoodsDto
import com.example.grabthisforme.model.secondhandGoods.data.dto.SecondhandTradeDto
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandTradeInfo
import com.example.grabthisforme.model.user.domain.User

private fun buildSaleUserOrNull(saleUserId: Long?): User? {
    return saleUserId?.let { userId ->
        User(
            id = userId,
            name = "",
            headPic = ""
        )
    }
}

fun SecondhandTradeDto.toDomainInfo(): SecondhandTradeInfo {
    return SecondhandTradeInfo(
        saleUser = buildSaleUserOrNull(saleUserId),
        originalPrice = originalPrice,
        quality = quality,
        usedTime = usedTime,
        tradeStatus = tradeStatus,
        negotiable = negotiable
    )
}

fun SecondhandTradeInfo.toDto(): SecondhandTradeDto {
    return SecondhandTradeDto(
        saleUserId = saleUser?.id,
        originalPrice = originalPrice,
        quality = quality,
        usedTime = usedTime,
        tradeStatus = tradeStatus,
        negotiable = negotiable
    )
}

fun SecondhandTradeInfo.toEntity(goodsId: Long): SecondhandTradeEntity {
    return SecondhandTradeEntity(
        goodsId = goodsId,
        saleUserId = saleUser?.id,
        originalPrice = originalPrice,
        quality = quality,
        usedTime = usedTime,
        tradeStatus = tradeStatus,
        negotiable = negotiable
    )
}

fun SecondhandTradeEntity.toDomainInfo(): SecondhandTradeInfo {
    return SecondhandTradeInfo(
        saleUser = buildSaleUserOrNull(saleUserId),
        originalPrice = originalPrice,
        quality = quality,
        usedTime = usedTime,
        tradeStatus = tradeStatus,
        negotiable = negotiable
    )
}

fun SecondhandGoodsDto.toDomain(): SecondhandGoods {
    val goodsDomain = goods.toDomain()
    val tradeDomain = trade.toDomainInfo()
    return SecondhandGoods(
        saleUser = tradeDomain.saleUser,
        id = goodsDomain.id,
        name = goodsDomain.name,
        message = goodsDomain.message,
        category = goodsDomain.category ?: com.example.grabthisforme.model.goods.domain.Goods.GoodsCategory.CLOTHING,
        secondhandPrice = goodsDomain.price,
        sale_number = goodsDomain.sale_number,
        pic = goodsDomain.pic,
        originalPrice = tradeDomain.originalPrice,
        quality = tradeDomain.quality,
        usedTime = tradeDomain.usedTime,
        tradeStatus = tradeDomain.tradeStatus,
        negotiable = tradeDomain.negotiable,
        purchaseStatus = goodsDomain.purchaseStatus,
        soldCount = goodsDomain.soldCount
    )
}

fun SecondhandGoods.toDto(): SecondhandGoodsDto {
    return SecondhandGoodsDto(
        goods = (this as com.example.grabthisforme.model.goods.domain.Goods).toDto(),
        trade = tradeInfo.toDto()
    )
}

fun SecondhandGoods.toTradeEntity(): SecondhandTradeEntity {
    return tradeInfo.toEntity(id)
}
