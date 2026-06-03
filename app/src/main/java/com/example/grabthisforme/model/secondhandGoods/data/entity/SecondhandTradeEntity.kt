package com.example.grabthisforme.model.secondhandGoods.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.grabthisforme.model.goods.data.entity.GoodsBaseEntity
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandTradeInfo

@Entity(
    tableName = "secondhand_trade",
    foreignKeys = [
        ForeignKey(
            entity = GoodsBaseEntity::class,
            parentColumns = ["goodsId"],
            childColumns = ["goodsId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goodsId"], unique = true)]
)
data class SecondhandTradeEntity(
    @PrimaryKey val goodsId: Long,
    val saleUserId: Long? = null,
    val originalPrice: Double = 0.0,
    val quality: String = "",
    val usedTime: String? = null,
    val tradeStatus: Int = SecondhandTradeInfo.STATUS_ON_SALE,
    val negotiable: Boolean = true
)
