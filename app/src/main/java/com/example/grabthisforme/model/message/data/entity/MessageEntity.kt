package com.example.grabthisforme.model.message.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.grabthisforme.model.conversation.data.entity.ConversationEntity
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity

@Entity(
    tableName = "message_content",
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
            childColumns = ["senderId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["senderId"])
    ]
)
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderId: Long? = null,
    val type: String,
    val content: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long,
    val status: String
)
