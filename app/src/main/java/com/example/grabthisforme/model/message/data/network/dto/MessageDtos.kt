package com.example.grabthisforme.model.message.data.network.dto

data class MessageDto(
    val clientMsgId: String? = null,
    val messageId: String,
    val senderId: Long = 0L,
    val type: String,
    val content: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long,
    val status: String
)
