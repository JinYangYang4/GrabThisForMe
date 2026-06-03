package com.example.grabthisforme.model.friendAndGroup.data.repository

import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContactDirectoryState(
    val friends: List<Friend>,
    val groups: List<Group>,
    val connectedFriendIds: Set<Long>,
    val joinedGroupIds: Set<Long>
) {
    fun findFriend(friendId: Long): Friend? = friends.firstOrNull { it.friendId == friendId }

    fun findGroup(groupId: Long): Group? = groups.firstOrNull { it.groupId == groupId }

    fun isFriendConnected(friendId: Long): Boolean = connectedFriendIds.contains(friendId)

    fun isGroupJoined(groupId: Long): Boolean = joinedGroupIds.contains(groupId)

    fun commonGroupsForUser(userId: Long): List<Group> {
        return groups.filter { group ->
            joinedGroupIds.contains(group.groupId) && group.members.any { member -> member.id == userId }
        }
    }
}

@Singleton
class ContactDirectoryRepository @Inject constructor(
    private val friendAndGroupRepository: FriendAndGroupRepository
) {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val directoryState: StateFlow<ContactDirectoryState> = combine(
        friendAndGroupRepository.allUsers,
        friendAndGroupRepository.currentUserFriends,
        friendAndGroupRepository.allGroups,
        friendAndGroupRepository.currentUserGroupIds
    ) { users, currentUserFriends, groups, joinedGroupIds ->
        val connectedFriendsById = currentUserFriends.associateBy { it.friendId }
        val connectedFriendIds = connectedFriendsById.keys
        ContactDirectoryState(
            friends = users.map { user ->
                connectedFriendsById[user.id] ?: Friend(
                    friendId = user.id,
                    who = user,
                    addedTime = 0L,
                    status = Friend.FriendStatus.PENDING
                )
            },
            groups = groups,
            connectedFriendIds = connectedFriendIds,
            joinedGroupIds = joinedGroupIds
        )
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = ContactDirectoryState(
            friends = emptyList(),
            groups = emptyList(),
            connectedFriendIds = emptySet(),
            joinedGroupIds = emptySet()
        )
    )

    fun addFriend(friendId: Long) {
        repositoryScope.launch {
            friendAndGroupRepository.addFriend(friendId)
        }
    }

    fun removeFriend(friendId: Long) {
        repositoryScope.launch {
            friendAndGroupRepository.removeFriend(friendId)
        }
    }

    fun joinGroup(groupId: Long) {
        repositoryScope.launch {
            friendAndGroupRepository.joinGroup(groupId)
        }
    }

    fun leaveGroup(groupId: Long) {
        repositoryScope.launch {
            friendAndGroupRepository.leaveGroup(groupId)
        }
    }
}
