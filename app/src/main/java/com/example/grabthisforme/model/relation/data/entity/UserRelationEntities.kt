package com.example.grabthisforme.model.relation.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.grabthisforme.model.goods.data.entity.GoodsBaseEntity
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.store.data.entity.StoreEntity
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity

@Entity(
    tableName = "user_post",
    primaryKeys = ["userId", "postId"],
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["postId"], unique = true)
    ]
)
data class UserPostEntity(
    val userId: Long,
    val postId: String
)

@Entity(
    tableName = "user_liked_post",
    primaryKeys = ["userId", "postId"],
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["postId"])
    ]
)
data class UserLikedPostEntity(
    val userId: Long,
    val postId: String,
    val likedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_liked_store",
    primaryKeys = ["userId", "storeId"],
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["storeId"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["storeId"])
    ]
)
data class UserLikedStoreEntity(
    val userId: Long,
    val storeId: Long,
    val likedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_liked_goods",
    primaryKeys = ["userId", "goodsId"],
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
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
        Index(value = ["userId"]),
        Index(value = ["goodsId"])
    ]
)
data class UserLikedGoodsEntity(
    val userId: Long,
    val goodsId: Long,
    val likedAt: Long = System.currentTimeMillis()
)
