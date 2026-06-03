package com.example.grabthisforme.model.friendAndGroup.mapper

import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.entity.ChatGroupEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserGroupRelationEntity
import com.example.grabthisforme.model.user.domain.User

fun UserFriendRelationEntity.toDomain(friendUser: User): Friend {
    return Friend(
        friendId = friendUserId,
        who = friendUser,
        addedTime = addedTime,
        status = when (status) {
            Friend.FriendStatus.ACCEPTED.name -> Friend.FriendStatus.ACCEPTED
            Friend.FriendStatus.REJECTED.name -> Friend.FriendStatus.REJECTED
            else -> Friend.FriendStatus.PENDING
        }
    )
}

fun Group.toEntity(): ChatGroupEntity {
    return ChatGroupEntity(
        groupId = groupId,
        groupName = groupName,
        createTime = createTime
    )
}

fun ChatGroupEntity.toDomain(members: List<User>): Group {
    return Group(
        groupId = groupId,
        groupName = groupName,
        members = members,
        createTime = createTime
    )
}

fun UserGroupRelationEntity.isOwner(): Boolean {
    return role == UserGroupRelationEntity.OWNER_ROLE
}
