package com.example.grabthisforme.model.friendAndGroup.data.network.dto

data class ChatGroupDto(
    val groupId: Long,
    val groupName: String,
    val createTime: Long
)

data class UserFriendRelationDto(
    val userId: Long,
    val friendUserId: Long,
    val status: String,
    val addedTime: Long
)

data class UserGroupRelationDto(
    val userId: Long,
    val groupId: Long,
    val role: String,
    val joinedTime: Long
)

data class FriendAndGroupSeedDto(
    val groups: List<ChatGroupDto>,
    val friendRelations: List<UserFriendRelationDto>,
    val userGroupRelations: List<UserGroupRelationDto>
)
