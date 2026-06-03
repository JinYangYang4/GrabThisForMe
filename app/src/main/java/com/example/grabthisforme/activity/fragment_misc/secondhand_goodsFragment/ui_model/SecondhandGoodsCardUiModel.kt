package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.ui_model

import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import java.util.Locale

data class SecondhandGoodsCardUiModel(
    val goodsId: Long,
    val goodsName: String,
    val priceText: String,
    val originalPriceText: String,
    val qualityText: String,
    val messageText: String,
    val tradeHintText: String,
    val usedTimeText: String,
    val saleCountText: String,
    val imageUrl: String?
)

fun SecondhandGoods.toSecondhandGoodsCardUiModel(): SecondhandGoodsCardUiModel {
    val usedTimeValue = usedTime?.takeIf { it.isNotBlank() } ?: "上新不久"
    return SecondhandGoodsCardUiModel(
        goodsId = id,
        goodsName = name,
        priceText = String.format(Locale.getDefault(), "¥%.2f", price),
        originalPriceText = String.format(Locale.getDefault(), "¥%.2f", originalPrice),
        qualityText = quality,
        messageText = message,
        tradeHintText = if (negotiable) "支持小刀" else "一口价",
        usedTimeText = "使用 $usedTimeValue",
        saleCountText = "已售 $soldCount",
        imageUrl = pic.takeIf { it.isNotBlank() }
    )
}
