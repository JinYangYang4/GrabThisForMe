package com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model

import com.example.grabthisforme.model.goods.domain.Goods
import java.util.Locale

data class StoreOwnerGoodsItemUiModel(
    val goodsId: Long,
    val title: String,
    val priceText: String,
    val discountText: String,
    val stockText: String,
    val imageUrl: String?,
    val tags: List<String>
)

fun Goods.toStoreOwnerGoodsItemUiModel(): StoreOwnerGoodsItemUiModel {
    return StoreOwnerGoodsItemUiModel(
        goodsId = id,
        title = name,
        priceText = String.format(Locale.getDefault(), "¥%.2f", price),
        discountText = when {
            discountTag.isNotEmpty() -> discountTag
            discountPrice > 0 -> String.format(Locale.getDefault(), "优惠价：¥%.2f", discountPrice)
            else -> ""
        },
        stockText = if (stock > 0) "库存：$stock" else "售罄",
        imageUrl = pic.takeIf { it.isNotBlank() },
        tags = buildStoreGoodsTags(discountTag, tag)
    )
}
