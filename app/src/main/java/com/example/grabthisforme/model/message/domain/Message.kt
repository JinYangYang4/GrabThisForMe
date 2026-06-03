package com.example.grabthisforme.model.message.domain

data class Message(
    val messageId: String,
    val senderId: Long = 0L,
    val type: MessageType,
    val content: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long,
    val status: MessageStatus
) {
    enum class MessageType {
        TEXT,
        IMAGE,
        VOICE,
        SYSTEM
    }

    enum class MessageStatus {
        SENDING,
        SENT,
        FAILED,
        READ
    }
}
