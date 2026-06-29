package com.example.grabthisforme.model.friendAndGroup.data.repository

import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.local.dao.FriendAndGroupDao
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.local.entity.UserGroupRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.network.dto.GroupDto
import com.example.grabthisforme.model.friendAndGroup.mapper.toDomain
import com.example.grabthisforme.model.friendAndGroup.mapper.toEntity
import com.example.grabthisforme.model.user.data.local.dao.UserDao
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toDomain
import com.example.grabthisforme.model.user.mapper.toDomain as userDtoToDomain
import com.example.grabthisforme.model.user.data.network.dto.UserDto
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
class FriendAndGroupLocalRepository @Inject constructor(
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
        ensureUsersExist(group.members)
        friendAndGroupDao.upsertGroup(group.toEntity())
        ensureGroupExists(group.groupId, group.groupName)
    }

    suspend fun addFriend(friendUserId: Long) {
        val currentUserId = userRepository.currentUserId.value ?: return
        if (currentUserId == friendUserId) return
        val friendUser = userDao.getUserBasicBundlesByIds(listOf(friendUserId))
            .firstOrNull()
            ?.toDomain()
        if (friendUser == null) return
        val now = System.currentTimeMillis()
        val relation = UserFriendRelationEntity(
            userId = currentUserId,
            friendUserId = friendUserId,
            status = Friend.FriendStatus.ACCEPTED.name,
            addedTime = now
        )
        val reverseRelation = relation.copy(userId = friendUserId, friendUserId = currentUserId)
        ensureUsersExist(listOf(friendUser))
        friendAndGroupDao.upsertFriendRelations(listOf(relation, reverseRelation))
    }

    suspend fun syncCurrentUserFriends(friendUsers: List<UserDto>) {
        val currentUserId = userRepository.currentUserId.value ?: return
        val friendDomains = friendUsers.map { dto -> dto.userDtoToDomain() }
        ensureUsersExist(friendDomains)
        val existingRelations = friendAndGroupDao.getFriendRelationsByUserId(currentUserId)
        val existingByFriendId = existingRelations.associateBy { it.friendUserId }
        val remoteFriendIds = friendDomains.map { it.id }.toSet()

        val staleFriendIds = existingByFriendId.keys - remoteFriendIds
        if (staleFriendIds.isNotEmpty()) {
            friendAndGroupDao.deleteFriendRelations(currentUserId, staleFriendIds.toList())
        }

        if (friendDomains.isNotEmpty()) {
            val now = System.currentTimeMillis()
            val relationsToUpsert = friendDomains.map { friend ->
                val existing = existingByFriendId[friend.id]
                UserFriendRelationEntity(
                    userId = currentUserId,
                    friendUserId = friend.id,
                    status = Friend.FriendStatus.ACCEPTED.name,
                    addedTime = existing?.addedTime ?: now
                )
            }
            friendAndGroupDao.upsertFriendRelations(relationsToUpsert)
        }
    }

    suspend fun syncCurrentUserGroups(groups: List<GroupDto>) {
        val currentUserId = userRepository.currentUserId.value ?: return
        val allMembers = groups.flatMap { group ->
            group.members.mapNotNull { member -> member.user?.userDtoToDomain() }
        }
        ensureUsersExist(allMembers)
        val existingRelations = friendAndGroupDao.getUserGroupRelationsByUserId(currentUserId)
        val existingByGroupId = existingRelations.associateBy { it.groupId }
        val remoteGroupIds = groups.map { it.groupId }.toSet()

        val staleGroupIds = existingByGroupId.keys - remoteGroupIds
        if (staleGroupIds.isNotEmpty()) {
            friendAndGroupDao.deleteUserGroupRelations(currentUserId, staleGroupIds.toList())
            friendAndGroupDao.deleteGroupsByIds(staleGroupIds.toList())
        }

        if (groups.isNotEmpty()) {
            friendAndGroupDao.upsertGroups(
                groups.map { group ->
                    com.example.grabthisforme.model.friendAndGroup.data.local.entity.ChatGroupEntity(
                        groupId = group.groupId,
                        groupName = group.groupName,
                        createTime = group.createTime
                    )
                }
            )
            val currentUserRelations = groups.mapNotNull { group ->
                group.members.firstOrNull { member -> member.userId == currentUserId }?.let { member ->
                    val existing = existingByGroupId[group.groupId]
                    UserGroupRelationEntity(
                        userId = currentUserId,
                        groupId = group.groupId,
                        role = member.role,
                        joinedTime = existing?.joinedTime ?: member.joinedTime
                    )
                }
            }
            if (currentUserRelations.isNotEmpty()) {
                friendAndGroupDao.upsertUserGroupRelations(currentUserRelations)
            }
        }
    }

    suspend fun removeFriend(friendUserId: Long) {
        val currentUserId = userRepository.currentUserId.value ?: return
        friendAndGroupDao.deleteFriendRelation(currentUserId, friendUserId)
        friendAndGroupDao.deleteFriendRelation(friendUserId, currentUserId)
    }

    suspend fun joinGroup(groupId: Long, role: String = UserGroupRelationEntity.MEMBER_ROLE) {
        val currentUserId = userRepository.currentUserId.value ?: return
        ensureGroupExists(groupId)
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

    private suspend fun ensureUsersExist(users: List<User>) {
        userRepository.ensureCachedUsers(users)
    }

    private suspend fun ensureGroupExists(groupId: Long, groupName: String = "") {
        if (friendAndGroupDao.getGroupById(groupId) == null) {
            friendAndGroupDao.upsertGroup(
                Group(
                    groupId = groupId,
                    groupName = groupName.ifBlank { "群组" },
                    members = emptyList()
                ).toEntity()
            )
        }
    }
}
