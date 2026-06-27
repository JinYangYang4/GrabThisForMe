package com.example.grabthisforme.model.friendAndGroup.data.network.dto

import com.example.grabthisforme.model.user.data.network.dto.UserDto

data class GroupMemberDto(
    val userId: Long,
    val role: String,
    val joinedTime: Long,
    val user: UserDto? = null
)

data class GroupDto(
    val groupId: Long,
    val groupName: String,
    val createTime: Long,
    val members: List<GroupMemberDto> = emptyList()
)
