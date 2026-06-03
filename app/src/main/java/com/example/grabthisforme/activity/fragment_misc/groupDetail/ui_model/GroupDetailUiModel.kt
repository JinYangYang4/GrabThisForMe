package com.example.grabthisforme.activity.fragment_misc.groupDetail.ui_model

data class GroupDetailUiModel(
    val groupId: Long,
    val groupName: String,
    val memberCountText: String,
    val statusText: String,
    val primaryActionText: String,
    val secondaryActionText: String,
    val isJoined: Boolean,
    val sceneText: String,
    val managerText: String,
    val vibeText: String,
    val tipsText: String
)

data class GroupMemberItemUiModel(
    val userId: Long,
    val name: String,
    val subtitle: String,
    val isManager: Boolean
)
