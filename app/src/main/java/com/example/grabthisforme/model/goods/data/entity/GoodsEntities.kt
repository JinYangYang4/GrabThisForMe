package com.example.grabthisforme.model.goods.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity

@Entity(tableName = "goods_base")
data class GoodsBaseEntity(
    @PrimaryKey val goodsId: Long,
    val name: String,
    val message: String,
    val categoryKey: String? = null
)

@Entity(
    tableName = "goods_price",
    foreignKeys = [
        ForeignKey(
            entity = GoodsBaseEntity::class,
            parentColumns = ["goodsId"],
            childColumns = ["goodsId"],
            onDelete = ForeignKey.CASCADE  //删除商品时，价格自动删除
        )
    ],
    indices = [Index(value = ["goodsId"], unique = true)]
)
data class GoodsPriceEntity(
    @PrimaryKey val goodsId: Long,
    val price: Double,
    val discountPrice: Double = 0.0,
    val discountTag: String = ""
)

@Entity(
    tableName = "goods_ui",
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
data class GoodsUiEntity(
    @PrimaryKey val goodsId: Long,
    val pic: String = "",
    val tag: String = "",
    val unit: String = "",
    val selectedCount: Int = 0
)

@Entity(
    tableName = "goods_state",
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
data class GoodsStateEntity(
    @PrimaryKey val goodsId: Long,
    val saleNumber: Long = 0,
    val stock: Int = 0,
    val isSoldOut: Boolean = false,
    val isHot: Boolean = false,
    val purchaseStatus: Int = 0,
    val soldCount: Long = 0L
)

data class GoodsBundleEntity(
    @Embedded val base: GoodsBaseEntity,
    @Relation(
        parentColumn = "goodsId",
        entityColumn = "goodsId"
    )
    val price: GoodsPriceEntity?,
    @Relation(
        parentColumn = "goodsId",
        entityColumn = "goodsId"
    )
    val ui: GoodsUiEntity?,
    @Relation(
        parentColumn = "goodsId",
        entityColumn = "goodsId"
    )
    val state: GoodsStateEntity?,
    @Relation(
        parentColumn = "goodsId",
        entityColumn = "goodsId"
    )
    val trade: SecondhandTradeEntity?
)
