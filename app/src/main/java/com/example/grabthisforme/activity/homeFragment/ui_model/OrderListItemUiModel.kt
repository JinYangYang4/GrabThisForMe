package com.example.grabthisforme.activity.homeFragment.ui_model

import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderPurchaseInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OrderListItemUiModel(
    val orderId: String,
    val goodsName: String,
    val goodsMessage: String,
    val goodsPriceText: String,
    val shelfNumberText: String,
    val aimPositionText: String,
    val sendTimeText: String,
    val timeLeftText: String,
    val goodsImageUrl: String?,
    val buyerAvatarUrl: String?,
    val isBuyerSelf: Boolean,
    val statusBadgeText: String?
)

fun Order.toOrderListItemUiModel(currentTime: Long = System.currentTimeMillis()): OrderListItemUiModel {
    if (orderType == OrderPurchaseInfo.TYPE_PURCHASE) {
        return OrderListItemUiModel(
            orderId = orderId,
            goodsName = goods.name.ifBlank { "商品" },
            goodsMessage = goods.message.ifBlank { "暂无商品说明" },
            goodsPriceText = String.format(Locale.getDefault(), "单价 ￥%.2f", unitPrice),
            shelfNumberText = "数量：$quantity",
            aimPositionText = "店铺：${storeName.ifBlank { "未知店铺" }}",
            sendTimeText = "购买时间：${formatOrderTime(startTime)}",
            timeLeftText = if (discountAmount > 0) {
                String.format(Locale.getDefault(), "优惠 ￥%.2f · 实付 ￥%.2f", discountAmount, totalAmount)
            } else {
                String.format(Locale.getDefault(), "实付 ￥%.2f", totalAmount)
            },
            goodsImageUrl = goods.pic.takeIf { it.isNotBlank() },
            buyerAvatarUrl = buyer.headPic.takeIf { it.isNotBlank() },
            isBuyerSelf = true,
            statusBadgeText = "已支付"
        )
    }
    val expired = currentTime > endTime
    val goodsNameText = goods.name.ifBlank { "待采购商品" }
    val goodsMessageText = goods.message.ifBlank { "暂无补充说明" }
    val shelfText = shelf_number.ifBlank { "未填写" }
    val aimText = aim_position.ifBlank { "待确认" }

    return OrderListItemUiModel(
        orderId = orderId,
        goodsName = goodsNameText,
        goodsMessage = goodsMessageText,
        goodsPriceText = String.format(Locale.getDefault(), "商品价 ¥%.2f", goods.price),
        shelfNumberText = "货架：$shelfText",
        aimPositionText = "送达：$aimText",
        sendTimeText = "配送：${formatOrderTime(startTime)} - ${formatOrderTime(endTime)}",
        timeLeftText = buildTimeLeftText(startTime, endTime),
        goodsImageUrl = goods.pic.takeIf { it.isNotBlank() },
        buyerAvatarUrl = buyer.headPic.takeIf { it.isNotBlank() },
        isBuyerSelf = isBuyerSelf,
        statusBadgeText = when {
            expired -> "已完成"
            isBuyerSelf -> "待收货"
            else -> "待送货"
        }
    )
}

private fun formatOrderTime(timeStamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(timeStamp))
    } catch (_: Exception) {
        "时间异常"
    }
}

private fun buildTimeLeftText(startTime: Long, endTime: Long): String {
    val duration = endTime - startTime
    if (duration <= 0) return "已送达"

    val hours = duration / (1000 * 60 * 60)
    val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 && minutes > 0 -> "剩余 ${hours} 小时 ${minutes} 分钟"
        hours > 0 -> "剩余 ${hours} 小时"
        else -> "剩余 ${minutes} 分钟"
    }
}
