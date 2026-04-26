package com.example.grabthisforme.model.conversation.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.grabthisforme.model.messageContent.data.entity.MessageEntity

@Entity(
    tableName = "conversation",
    indices = [Index(value = ["lastMessageId"])]
)
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val type: String,
    val peerType: String,
    val peerUserIdsCsv: String = "",
    val unreadCount: Int,
    val lastMessageId: String,
    val lastTime: Long
)

data class ConversationBundleEntity(
    @Embedded val conversation: ConversationEntity,
    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "messageId"
    )
    val lastMessage: MessageEntity?
)
