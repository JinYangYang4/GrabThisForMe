package com.example.grabthisforme.model.conversation.data.network.dto

import com.example.grabthisforme.model.message.data.network.dto.MessageDto

data class ConversationParticipantDto(
    val id: Long,
    val accountName: String? = null,
    val name: String? = null,
    val headPic: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val gender: Int? = null,
    val isVip: Boolean? = null,
    val signature: String? = null,
    val createTime: Long? = null,
    val lastLoginTime: Long? = null
)

data class ConversationSocketPayloadDto(
    val type: String,
    val conversationId: String,
    val message: MessageDto? = null
)
