package com.example.grabthisforme.model.friendAndGroup.data.repository

import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.dao.FriendAndGroupDao
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserGroupRelationEntity
import com.example.grabthisforme.model.friendAndGroup.mapper.toDomain
import com.example.grabthisforme.model.friendAndGroup.mapper.toEntity
import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class FriendAndGroupRepository @Inject constructor(
    private val friendAndGroupDao: FriendAndGroupDao,
    private val userDao: UserDao,
    private val userRepository: UserRepository
) {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allUsers: StateFlow<List<User>> = combine(
        userDao.observeAllUserBasicBundles(),
        userRepository.currentUserId
    ) { bundles, currentUserId ->
        bundles.map { it.toDomain() }
            .filter { user -> user.id != currentUserId }
            .sortedBy { user -> user.name }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val currentUserFriends: StateFlow<List<Friend>> = userRepository.currentUserId
        .flatMapLatest { currentUserId ->
            if (currentUserId == null) {
                flowOf(emptyList())
            } else {
                friendAndGroupDao.observeFriendRelationsByUserId(currentUserId)
                    .flatMapLatest { relations ->
                        val acceptedRelations = relations.filter { relation ->
                            relation.friendUserId != currentUserId &&
                                relation.status == Friend.FriendStatus.ACCEPTED.name
                        }
                        val friendIds = acceptedRelations.map { it.friendUserId }.distinct()
                        if (friendIds.isEmpty()) {
                            flowOf(emptyList())
                        } else {
                            userDao.observeUserBasicBundlesByIds(friendIds)
                                .map { bundles ->
                                    val usersById = bundles.map { it.toDomain() }.associateBy { it.id }
                                    acceptedRelations.mapNotNull { relation ->
                                        usersById[relation.friendUserId]?.let(relation::toDomain)
                                    }
                                }
                        }
                    }
            }
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val currentUserFriendIds: StateFlow<Set<Long>> = currentUserFriends
        .map { friends -> friends.map { it.friendId }.toSet() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )

    val allGroups: StateFlow<List<Group>> = combine(
        friendAndGroupDao.observeAllGroups(),
        friendAndGroupDao.observeAllUserGroupRelations()
    ) { groups, relations ->
        groups to relations
    }.flatMapLatest { (groups, relations) ->
        val userIds = relations.map { it.userId }.distinct()
        if (userIds.isEmpty()) {
            flowOf(groups.map { it.toDomain(emptyList()) })
        } else {
            userDao.observeUserBasicBundlesByIds(userIds).map { bundles ->
                val usersById = bundles.map { it.toDomain() }.associateBy { it.id }
                val relationsByGroupId = relations.groupBy { it.groupId }
                groups.map { group ->
                    val members = relationsByGroupId[group.groupId]
                        .orEmpty()
                        .sortedBy { it.joinedTime }
                        .mapNotNull { memberRelation -> usersById[memberRelation.userId] }
                    group.toDomain(members)
                }
            }
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val currentUserGroups: StateFlow<List<Group>> = combine(
        allGroups,
        userRepository.currentUserId.flatMapLatest { currentUserId ->
            if (currentUserId == null) {
                flowOf(emptyList())
            } else {
                friendAndGroupDao.observeUserGroupRelationsByUserId(currentUserId)
            }
        }
    ) { groups, memberships ->
        val joinedGroupIds = memberships.map { it.groupId }.toSet()
        groups.filter { it.groupId in joinedGroupIds }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val currentUserGroupIds: StateFlow<Set<Long>> = currentUserGroups
        .map { groups -> groups.map { it.groupId }.toSet() }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )
    suspend fun upsertGroup(group: Group) {
        friendAndGroupDao.upsertGroup(group.toEntity())
    }

    suspend fun addFriend(friendUserId: Long) {
        val currentUserId = userRepository.currentUserId.value ?: return
        if (currentUserId == friendUserId) return
        val now = System.currentTimeMillis()
        val relation = UserFriendRelationEntity(
            userId = currentUserId,
            friendUserId = friendUserId,
            status = Friend.FriendStatus.ACCEPTED.name,
            addedTime = now
        )
        val reverseRelation = relation.copy(userId = friendUserId, friendUserId = currentUserId)
        friendAndGroupDao.upsertFriendRelations(listOf(relation, reverseRelation))
    }

    suspend fun removeFriend(friendUserId: Long) {
        val currentUserId = userRepository.currentUserId.value ?: return
        friendAndGroupDao.deleteFriendRelation(currentUserId, friendUserId)
        friendAndGroupDao.deleteFriendRelation(friendUserId, currentUserId)
    }

    suspend fun joinGroup(groupId: Long, role: String = UserGroupRelationEntity.MEMBER_ROLE) {
        val currentUserId = userRepository.currentUserId.value ?: return
        friendAndGroupDao.upsertUserGroupRelation(
            UserGroupRelationEntity(
                userId = currentUserId,
                groupId = groupId,
                role = role,
                joinedTime = System.currentTimeMillis()
            )
        )
    }

    suspend fun leaveGroup(groupId: Long) {
        val currentUserId = userRepository.currentUserId.value ?: return
        friendAndGroupDao.deleteUserGroupRelation(currentUserId, groupId)
    }
}
