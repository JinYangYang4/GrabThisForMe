package com.example.grabthisforme.model.messageContent.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "message_content",
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val conversationId: String,
    val senderId: Long = 0L,
    val type: String,
    val content: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long,
    val need_show_time: Boolean = false,
    val isMine: Boolean,
    val status: String
)
