package com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model

import com.example.grabthisforme.model.goods.domain.Goods
import java.util.Locale

data class StoreGoodsListItemUiModel(
    val goodsId: Long,
    val title: String,
    val price: Double,
    val priceText: String,
    val discountText: String,
    val imageUrl: String?,
    val tags: List<String>
)

fun Goods.toStoreGoodsListItemUiModel(): StoreGoodsListItemUiModel {
    return StoreGoodsListItemUiModel(
        goodsId = id,
        title = name,
        price = price,
        priceText = String.format(Locale.getDefault(), "¥%.2f", price),
        discountText = when {
            discountTag.isNotEmpty() -> discountTag
            discountPrice > 0 -> String.format(Locale.getDefault(), "优惠价 ¥%.2f", discountPrice)
            else -> ""
        },
        imageUrl = pic.takeIf { it.isNotBlank() },
        tags = buildStoreGoodsTags(discountTag, tag)
    )
}

fun StoreGoodsListItemUiModel.toSelectedGoodsItemUiModel(selectedCount: Int = 1): SelectedGoodsItemUiModel {
    return SelectedGoodsItemUiModel(
        goodsId = goodsId,
        title = title,
        price = price,
        priceText = priceText,
        discountText = discountText,
        imageUrl = imageUrl,
        tags = tags,
        selectedCount = selectedCount
    )
}

internal fun buildStoreGoodsTags(discountTag: String, tag: String): List<String> {
    val tags = mutableListOf<String>()
    if (discountTag.isNotBlank()) {
        tags.add(discountTag)
    }
    if (tag.isNotBlank()) {
        tags.addAll(tag.split(Regex("[,/;|\\s]+")).filter { it.isNotBlank() })
    }
    if (tags.isEmpty()) {
        tags.add("默认")
    }
    return tags.distinct()
}
