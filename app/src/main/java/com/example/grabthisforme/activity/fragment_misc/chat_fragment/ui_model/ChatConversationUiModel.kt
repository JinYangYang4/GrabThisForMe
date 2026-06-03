package com.example.grabthisforme.activity.fragment_misc.chat_fragment.ui_model

data class ChatConversationUiModel(
    val title: String,
    val subtitle: String,
    val avatarUrl: String?,
    val isGroup: Boolean,
    val userDetailId: Long? = null,
    val groupDetailId: Long? = null
)
