package com.example.grabthisforme.model.friendAndGroup.data.repository
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.UserGroupRelationEntity
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class FriendAndGroupRepository @Inject constructor(
    private val localRepository: FriendAndGroupLocalRepository,
    private val remoteRepository: FriendAndGroupRemoteRepository,
    private val conversationRepository: ConversationRepository,
    private val userRepository: UserRepository
) {
    val allUsers: StateFlow<List<com.example.grabthisforme.model.user.domain.User>> = localRepository.allUsers
    val currentUserFriends: StateFlow<List<Friend>> = localRepository.currentUserFriends
    val currentUserPendingFriendRequests: StateFlow<List<Friend>> = localRepository.currentUserPendingFriendRequests
    val currentUserFriendIds: StateFlow<Set<Long>> = localRepository.currentUserFriendIds
    val allGroups: StateFlow<List<Group>> = localRepository.allGroups
    val currentUserGroups: StateFlow<List<Group>> = localRepository.currentUserGroups
    val currentUserGroupIds: StateFlow<Set<Long>> = localRepository.currentUserGroupIds

    suspend fun upsertGroup(group: Group) {
        localRepository.upsertGroup(group)
    }

    suspend fun addFriend(friendUserId: Long, friendUser: User? = null) {
        remoteRepository.addFriend(friendUserId)
            .onSuccess {
                refreshRemoteFriends()
                refreshRemoteFriendRequests()
            }
            .onFailure {
            }
    }

    suspend fun removeFriend(friendUserId: Long) {
        localRepository.removeFriend(friendUserId)
    }

    suspend fun joinGroup(groupId: Long, role: String = UserGroupRelationEntity.MEMBER_ROLE) {
        remoteRepository.joinGroup(groupId)
            .onSuccess {
                refreshRemoteGroups()
                conversationRepository.refreshRemoteConversations()
            }
    }

    suspend fun leaveGroup(groupId: Long) {
        localRepository.leaveGroup(groupId)
    }

    suspend fun refreshRemoteFriends() {
        remoteRepository.listFriends()
            .onSuccess { friends ->
                localRepository.syncCurrentUserFriends(friends)
            }
    }

    suspend fun refreshRemoteFriendRequests() {
        remoteRepository.listFriendRequests()
            .onSuccess { requests ->
                localRepository.syncCurrentUserFriendRequests(requests)
            }
    }

    suspend fun acceptFriendRequest(friendUserId: Long) {
        remoteRepository.acceptFriend(friendUserId)
            .onSuccess {
                refreshRemoteFriendRequests()
                refreshRemoteFriends()
                conversationRepository.refreshRemoteConversations()
            }
    }

    suspend fun refreshRemoteGroups() {
        remoteRepository.listGroups()
            .onSuccess { groups ->
                localRepository.syncCurrentUserGroups(groups)
            }
    }
}

