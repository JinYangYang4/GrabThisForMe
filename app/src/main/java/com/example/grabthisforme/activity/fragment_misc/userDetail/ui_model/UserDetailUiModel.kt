package com.example.grabthisforme.activity.fragment_misc.userDetail.ui_model

data class UserDetailUiModel(
    val userId: Long,
    val name: String,
    val signature: String,
    val phoneText: String,
    val genderText: String,
    val statusText: String,
    val primaryActionText: String,
    val secondaryActionText: String,
    val isConnected: Boolean,
    val accountHint: String,
    val campusHint: String,
    val activityHint: String,
    val groupSummary: String
)

data class UserCommonGroupItemUiModel(
    val groupId: Long,
    val title: String,
    val subtitle: String
)
