package com.example.grabthisforme.model.conversation.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.grabthisforme.model.message.data.local.entity.MessageEntity
import com.example.grabthisforme.model.user.data.local.entity.UserAccountEntity

@Entity(
    tableName = "conversation",
    indices = [
        Index(value = ["lastMessageId"]),
        Index(value = ["conversationType", "targetId"], unique = true)
    ]
)
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val conversationType: String,
    val targetId: Long? = null,
    val lastMessageId: String? = null,
    val lastTime: Long
)

@Entity(
    tableName = "conversation_user_state",
    primaryKeys = ["conversationId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["conversationId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["userId"])
    ]
)
data class ConversationUserStateEntity(
    val conversationId: String,
    val userId: Long,
    val unreadCount: Int = 0,
    val isHidden: Boolean = false,
    val lastReadTime: Long? = null
)

data class ConversationBundleEntity(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "messageId"
    )
    val lastMessage: MessageEntity?
)
