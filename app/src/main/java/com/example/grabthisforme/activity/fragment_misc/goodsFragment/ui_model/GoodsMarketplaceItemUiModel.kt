package com.example.grabthisforme.activity.fragment_misc.goodsFragment.ui_model

import com.example.grabthisforme.model.goods.domain.Goods
import java.util.Locale

data class GoodsMarketplaceItemUiModel(
    val goodsId: Long,
    val goodsName: String,
    val goodsMessage: String,
    val priceText: String,
    val discountPriceText: String,
    val discountTag: String,
    val showDiscountPrice: Boolean,
    val showDiscountTag: Boolean,
    val storeTypeText: String,
    val heatText: String,
    val statusText: String,
    val imageUrl: String?,
    val isHot: Boolean
)

fun Goods.toGoodsMarketplaceItemUiModel(): GoodsMarketplaceItemUiModel {
    val discountVisible = discountPrice > 0 && discountPrice < price
    return GoodsMarketplaceItemUiModel(
        goodsId = id,
        goodsName = name,
        goodsMessage = message,
        priceText = String.format(Locale.getDefault(), "¥%.1f", price),
        discountPriceText = String.format(Locale.getDefault(), "到手 ¥%.1f", discountPrice),
        discountTag = discountTag,
        showDiscountPrice = discountVisible,
        showDiscountTag = discountTag.isNotBlank(),
        storeTypeText = resolveMarketplaceStoreType(tag),
        heatText = if (isHot) "热卖中" else "新上架",
        statusText = if (isSoldOut) "暂时售罄" else "支持下单",
        imageUrl = pic.takeIf { it.isNotBlank() },
        isHot = isHot
    )
}

private fun resolveMarketplaceStoreType(tag: String): String {
    return when {
        "便利店" in tag -> "便利店"
        "咖啡店" in tag -> "咖啡轻食"
        "打印店" in tag -> "打印店"
        "工作室" in tag -> "工作室"
        "生活馆" in tag -> "生活馆"
        "文具店" in tag -> "文具店"
        "运动馆" in tag -> "运动馆"
        "数码店" in tag -> "数码店"
        else -> "校园好店"
    }
}
