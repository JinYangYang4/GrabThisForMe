package com.example.grabthisforme.model.goods.mapper

import com.example.grabthisforme.model.goods.data.dto.GoodsDto
import com.example.grabthisforme.model.goods.data.dto.GoodsBaseDto
import com.example.grabthisforme.model.goods.data.dto.GoodsPriceDto
import com.example.grabthisforme.model.goods.data.dto.GoodsStateDto
import com.example.grabthisforme.model.goods.data.dto.GoodsUiDto
import com.example.grabthisforme.model.goods.data.entity.GoodsBaseEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsBundleEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsPriceEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsStateEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsUiEntity
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.goods.domain.GoodsBaseInfo
import com.example.grabthisforme.model.goods.domain.GoodsPriceInfo
import com.example.grabthisforme.model.goods.domain.GoodsStateInfo
import com.example.grabthisforme.model.goods.domain.GoodsUiInfo
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandTradeInfo
import com.example.grabthisforme.model.user.User

fun Goods.toBaseEntity(): GoodsBaseEntity {
    return GoodsBaseEntity(
        goodsId = id,
        name = name,
        message = message,
        categoryKey = category?.name
    )
}

fun Goods.toPriceEntity(): GoodsPriceEntity {
    return GoodsPriceEntity(
        goodsId = id,
        price = price,
        discountPrice = discountPrice,
        discountTag = discountTag
    )
}

fun Goods.toUiEntity(): GoodsUiEntity {
    return GoodsUiEntity(
        goodsId = id,
        pic = pic,
        tag = tag,
        unit = unit,
        selectedCount = selectedCount
    )
}

fun Goods.toStateEntity(): GoodsStateEntity {
    return GoodsStateEntity(
        goodsId = id,
        saleNumber = sale_number,
        stock = stock,
        isSoldOut = isSoldOut,
        isHot = isHot
    )
}

fun GoodsBundleEntity.toDomain(): Goods {
    val category = base.categoryKey?.let { key ->
        Goods.GoodsCategory.entries.firstOrNull { it.name == key }
    }
    return Goods(
        id = base.goodsId,
        name = base.name,
        message = base.message,
        price = price?.price ?: 0.0,
        sale_number = state?.saleNumber ?: 0,
        pic = ui?.pic.orEmpty(),
        category = category,
        discountPrice = price?.discountPrice ?: 0.0,
        discountTag = price?.discountTag.orEmpty(),
        tag = ui?.tag.orEmpty(),
        stock = state?.stock ?: 0,
        isSoldOut = state?.isSoldOut ?: false,
        isHot = state?.isHot ?: false,
        unit = ui?.unit.orEmpty(),
        selectedCount = ui?.selectedCount ?: 0
    )
}

fun GoodsBundleEntity.toDomainSecondhandOrNull(): SecondhandGoods? {
    val tradeEntity = trade ?: return null
    val seller = if (tradeEntity.saleUserId != null || tradeEntity.saleUserName.isNotBlank() || tradeEntity.saleUserAvatar.isNotBlank()) {
        User(
            id = tradeEntity.saleUserId ?: 0L,
            name = tradeEntity.saleUserName.ifBlank { "匿名用户" },
            headPic = tradeEntity.saleUserAvatar
        )
    } else {
        null
    }

    return SecondhandGoods(
        saleUser = seller,
        id = base.goodsId,
        name = base.name,
        message = base.message,
        secondhandPrice = price?.price ?: 0.0,
        sale_number = state?.saleNumber ?: 0,
        pic = ui?.pic.orEmpty(),
        originalPrice = tradeEntity.originalPrice,
        quality = tradeEntity.quality,
        usedTime = tradeEntity.usedTime,
        tradeStatus = tradeEntity.tradeStatus,
        negotiable = tradeEntity.negotiable
    )
}

fun GoodsBaseDto.toDomainInfo(): GoodsBaseInfo {
    return GoodsBaseInfo(
        id = id,
        name = name,
        message = message,
        category = category?.let { key ->
            Goods.GoodsCategory.entries.firstOrNull { it.name == key || it.desc == key }
        }
    )
}

fun GoodsPriceDto.toDomainInfo(): GoodsPriceInfo {
    return GoodsPriceInfo(
        price = price,
        discountPrice = discountPrice,
        discountTag = discountTag
    )
}

fun GoodsUiDto.toDomainInfo(): GoodsUiInfo {
    return GoodsUiInfo(
        pic = pic,
        tag = tag,
        unit = unit,
        selectedCount = selectedCount
    )
}

fun GoodsStateDto.toDomainInfo(): GoodsStateInfo {
    return GoodsStateInfo(
        saleNumber = saleNumber,
        stock = stock,
        isSoldOut = isSoldOut,
        isHot = isHot
    )
}

fun GoodsDto.toDomain(): Goods {
    return Goods(
        baseInfo = base.toDomainInfo(),
        priceInfo = price.toDomainInfo(),
        uiInfo = ui.toDomainInfo(),
        stateInfo = state.toDomainInfo()
    )
}

fun Goods.toDto(): GoodsDto {
    return GoodsDto(
        base = GoodsBaseDto(
            id = id,
            name = name,
            message = message,
            category = category?.name
        ),
        price = GoodsPriceDto(
            price = price,
            discountPrice = discountPrice,
            discountTag = discountTag
        ),
        ui = GoodsUiDto(
            pic = pic,
            tag = tag,
            unit = unit,
            selectedCount = selectedCount
        ),
        state = GoodsStateDto(
            saleNumber = sale_number,
            stock = stock,
            isSoldOut = isSoldOut,
            isHot = isHot
        )
    )
}

fun Goods.toSecondhandTradeEntity(
    saleUser: User? = null,
    originalPrice: Double = price,
    quality: String = "",
    usedTime: String? = null,
    tradeStatus: Int = SecondhandTradeInfo.STATUS_ON_SALE,
    negotiable: Boolean = true
): SecondhandTradeEntity {
    return SecondhandTradeEntity(
        goodsId = id,
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

