package com.example.grabthisforme.model.friendAndGroup.data.repository

import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.UserGroupRelationEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FriendAndGroupRepository @Inject constructor(
    private val localRepository: FriendAndGroupLocalRepository,
    private val remoteRepository: FriendAndGroupRemoteRepository
) {
    val allUsers: StateFlow<List<com.example.grabthisforme.model.user.domain.User>> = localRepository.allUsers
    val currentUserFriends: StateFlow<List<Friend>> = localRepository.currentUserFriends
    val currentUserFriendIds: StateFlow<Set<Long>> = localRepository.currentUserFriendIds
    val allGroups: StateFlow<List<Group>> = localRepository.allGroups
    val currentUserGroups: StateFlow<List<Group>> = localRepository.currentUserGroups
    val currentUserGroupIds: StateFlow<Set<Long>> = localRepository.currentUserGroupIds

    suspend fun upsertGroup(group: Group) {
        localRepository.upsertGroup(group)
    }

    suspend fun addFriend(friendUserId: Long) {
        localRepository.addFriend(friendUserId)
    }

    suspend fun removeFriend(friendUserId: Long) {
        localRepository.removeFriend(friendUserId)
    }

    suspend fun joinGroup(groupId: Long, role: String = UserGroupRelationEntity.MEMBER_ROLE) {
        localRepository.joinGroup(groupId, role)
    }

    suspend fun leaveGroup(groupId: Long) {
        localRepository.leaveGroup(groupId)
    }
}
