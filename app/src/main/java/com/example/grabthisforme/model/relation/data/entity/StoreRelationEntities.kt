package com.example.grabthisforme.model.relation.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.grabthisforme.model.goods.data.local.entity.GoodsBaseEntity
import com.example.grabthisforme.model.store.data.local.entity.StoreEntity

@Entity(
    tableName = "store_goods_group",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["storeId"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["storeId"])
    ]
)
data class StoreGoodsCategoryEntity(
    @PrimaryKey val groupId: Long,
    val storeId: Long,
    val category: String,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "store_goods_group_item",
    primaryKeys = ["groupId", "goodsId"],
    foreignKeys = [
        ForeignKey(
            entity = StoreGoodsCategoryEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GoodsBaseEntity::class,
            parentColumns = ["goodsId"],
            childColumns = ["goodsId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["goodsId"])
    ]
)
data class StoreGoodsCategoryItemEntity(
    val groupId: Long,
    val goodsId: Long,
    val sortOrder: Int = 0
)

@Entity(
    tableName = "store_tag",
    primaryKeys = ["storeId", "tag"],
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["storeId"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["storeId"])
    ]
)
data class StoreTagEntity(
    val storeId: Long,
    val tag: String,
    val sortOrder: Int = 0
)
