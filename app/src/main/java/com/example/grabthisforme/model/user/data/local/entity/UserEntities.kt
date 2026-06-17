package com.example.grabthisforme.model.user.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.relation.data.entity.UserPostEntity
import com.example.grabthisforme.model.user.domain.UserProfile

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val userId: Long,
    val accountName: String,
    val passwordHash: String = "",
    val isCurrent: Boolean = false,
    val isLoginAccount: Boolean = true,
    val createTime: Long = System.currentTimeMillis(),
    val lastLoginTime: Long? = null
)

@Entity(
    tableName = "user_profile",
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey val userId: Long,
    val displayName: String,
    val avatarUrl: String = "",
    val phone: String? = null,
    val email: String? = null,
    val gender: Int = UserProfile.GENDER_UNKNOWN,
    val isVip: Boolean = false,
    val signature: String? = null
)

@Entity(
    tableName = "user_statistics",
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserStatisticsEntity(
    @PrimaryKey val userId: Long,
    val likeCount: Long = 0L,
    val fanCount: Long = 0L,
    val followCount: Long = 0L
)

data class UserBasicBundleEntity(
    @Embedded val account: UserAccountEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val profile: UserProfileEntity?,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val statistics: UserStatisticsEntity?
)

data class UserBundleEntity(
    @Embedded val account: UserAccountEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val profile: UserProfileEntity?,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val statistics: UserStatisticsEntity?,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val userPosts: List<UserPostEntity> = emptyList(),
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val likedPosts: List<UserLikedPostEntity> = emptyList(),
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val likedStores: List<UserLikedStoreEntity> = emptyList(),
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val likedGoods: List<UserLikedGoodsEntity> = emptyList()
)
