package com.example.grabthisforme.model.secondhandGoods.mapper

import com.example.grabthisforme.model.goods.data.dto.GoodsDto
import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDto
import com.example.grabthisforme.model.goods.mapper.toSecondhandTradeEntity
import com.example.grabthisforme.model.secondhandGoods.data.dto.SecondhandGoodsDto
import com.example.grabthisforme.model.secondhandGoods.data.dto.SecondhandTradeDto
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandTradeInfo
import com.example.grabthisforme.model.user.domain.User

fun SecondhandTradeDto.toDomainInfo(): SecondhandTradeInfo {
    val seller = if (saleUserId != null || saleUserName.isNotBlank() || saleUserAvatar.isNotBlank()) {
        User(
            id = saleUserId ?: 0L,
            name = saleUserName.ifBlank { "匿名用户" },
            headPic = saleUserAvatar
        )
    } else {
        null
    }

    return SecondhandTradeInfo(
        saleUser = seller,
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
        saleUserName = saleUser?.name.orEmpty(),
        saleUserAvatar = saleUser?.headPic.orEmpty(),
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
        saleUserName = saleUser?.name.orEmpty(),
        saleUserAvatar = saleUser?.headPic.orEmpty(),
        originalPrice = originalPrice,
        quality = quality,
        usedTime = usedTime,
        tradeStatus = tradeStatus,
        negotiable = negotiable
    )
}

fun SecondhandTradeEntity.toDomainInfo(): SecondhandTradeInfo {
    val seller = if (saleUserId != null || saleUserName.isNotBlank() || saleUserAvatar.isNotBlank()) {
        User(
            id = saleUserId ?: 0L,
            name = saleUserName.ifBlank { "匿名用户" },
            headPic = saleUserAvatar
        )
    } else {
        null
    }

    return SecondhandTradeInfo(
        saleUser = seller,
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
        secondhandPrice = goodsDomain.price,
        sale_number = goodsDomain.sale_number,
        pic = goodsDomain.pic,
        originalPrice = tradeDomain.originalPrice,
        quality = tradeDomain.quality,
        usedTime = tradeDomain.usedTime,
        tradeStatus = tradeDomain.tradeStatus,
        negotiable = tradeDomain.negotiable
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
