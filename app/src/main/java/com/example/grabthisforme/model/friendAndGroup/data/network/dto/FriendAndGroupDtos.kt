package com.example.grabthisforme.model.friendAndGroup.data.network.dto

import com.example.grabthisforme.model.user.data.network.dto.UserBriefDto

data class FriendRequestDto(
    val userId: Long,
    val status: String,
    val addedTime: Long,
    val user: UserBriefDto? = null
)

data class GroupMemberDto(
    val userId: Long,
    val role: String,
    val joinedTime: Long,
    val user: UserBriefDto? = null
)

data class GroupDto(
    val groupId: Long,
    val groupName: String,
    val createTime: Long,
    val members: List<GroupMemberDto> = emptyList()
)
