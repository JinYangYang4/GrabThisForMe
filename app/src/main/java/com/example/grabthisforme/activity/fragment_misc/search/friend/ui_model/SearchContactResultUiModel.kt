package com.example.grabthisforme.activity.fragment_misc.search.friend.ui_model

data class SearchContactResultUiModel(
    val stableId: String,
    val title: String,
    val subtitle: String,
    val badgeText: String,
    val statusText: String,
    val actionText: String?,
    val actionEnabled: Boolean,
    val isFriend: Boolean,
    val isConnected: Boolean,
    val connectedText: String? = null
)
