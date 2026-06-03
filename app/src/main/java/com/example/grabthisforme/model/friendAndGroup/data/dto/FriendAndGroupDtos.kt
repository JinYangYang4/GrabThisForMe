package com.example.grabthisforme.model.friendAndGroup.data.dto

import com.example.grabthisforme.model.friendAndGroup.data.entity.ChatGroupEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserGroupRelationEntity

data class FriendAndGroupSeedDto(
    val groups: List<ChatGroupEntity>,
    val friendRelations: List<UserFriendRelationEntity>,
    val userGroupRelations: List<UserGroupRelationEntity>
)
