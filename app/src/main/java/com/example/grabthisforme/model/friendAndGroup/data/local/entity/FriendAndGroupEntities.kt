package com.example.grabthisforme.model.friendAndGroup.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.grabthisforme.model.user.data.local.entity.UserAccountEntity

@Entity(
    tableName = "chat_group",
    indices = [Index(value = ["groupName"])]
)
data class ChatGroupEntity(
    @PrimaryKey val groupId: Long,
    val groupName: String,
    val createTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_friend_relation",
    primaryKeys = ["userId", "friendUserId"],
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["friendUserId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["friendUserId"])
    ]
)
data class UserFriendRelationEntity(
    val userId: Long,
    val friendUserId: Long,
    val status: String,
    val addedTime: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_group_relation",
    primaryKeys = ["userId", "groupId"],
    foreignKeys = [
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ChatGroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["groupId"])
    ]
)
data class UserGroupRelationEntity(
    val userId: Long,
    val groupId: Long,
    val role: String = MEMBER_ROLE,
    val joinedTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val OWNER_ROLE = "OWNER"
        const val MEMBER_ROLE = "MEMBER"
    }
}

data class ChatGroupWithMembersDto(
    @Embedded val group: ChatGroupEntity,
    @Relation(
        parentColumn = "groupId",
        entityColumn = "groupId"
    )
    val members: List<UserGroupRelationEntity>
)
