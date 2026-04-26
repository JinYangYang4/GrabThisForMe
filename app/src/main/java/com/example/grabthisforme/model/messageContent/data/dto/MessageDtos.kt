package com.example.grabthisforme.model.messageContent.data.dto

data class MessageDto(
    val messageId: String,
    val senderId: Long = 0L,
    val type: String,
    val content: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long,
    val need_show_time: Boolean = false,
    val isMine: Boolean,
    val status: String
)
