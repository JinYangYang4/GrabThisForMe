package com.example.grabthisforme.model.user

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "user_account")
data class UserAccountEntity(
    @PrimaryKey val userId: Long,
    val accountName: String,
    val passwordHash: String = "",
    val isCurrent: Boolean = false,
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

data class UserBundleEntity(
    @Embedded val account: UserAccountEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId"
    )
    val profile: UserProfileEntity?
)

