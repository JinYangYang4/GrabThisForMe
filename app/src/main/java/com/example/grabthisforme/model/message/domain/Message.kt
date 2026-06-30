package com.example.grabthisforme.model.message.domain

data class Message(
    val clientMsgId: String,
    val serverMsgId: String? = null,
    val senderId: Long = 0L,
    val type: MessageType,
    val content: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long,
    val serverTimestamp: Long? = null,
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
