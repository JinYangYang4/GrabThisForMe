package com.example.grabthisforme.model.conversation.data.repository

import com.example.grabthisforme.model.conversation.data.local.dao.ConversationDao
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationUserStateDao
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.conversation.mapper.buildPeerIds
import com.example.grabthisforme.model.conversation.mapper.toDomain
import com.example.grabthisforme.model.conversation.mapper.toEntity
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.relation.data.dao.ConversationRelationDao
import com.example.grabthisforme.model.relation.data.entity.ConversationParticipantEntity
import com.example.grabthisforme.model.user.data.local.dao.UserDao
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toDomain
import java.util.UUID
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationLocalRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val conversationUserStateDao: ConversationUserStateDao,
    private val conversationRelationDao: ConversationRelationDao,
    private val userDao: UserDao,
    private val userRepository: UserRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allConversations: StateFlow<List<Conversation>> = userRepository.currentUserId
        .flatMapLatest { currentUserId ->
            if (currentUserId == null) {
                flowOf(emptyList())
            } else {
                combine(
                    conversationDao.observeAllConversationBundles(),
                    conversationRelationDao.observeParticipantsByUserId(currentUserId),
                    conversationRelationDao.observeAllParticipants(),
                    conversationUserStateDao.observeStatesByUserId(currentUserId)
                ) { bundles, currentUserParticipations, allParticipants, states ->
                    Quadruple(bundles, currentUserParticipations, allParticipants, states)
                }.flatMapLatest { (bundles, currentUserParticipations, allParticipants, states) ->
                    flow {
                        val hiddenConversationIds = states
                            .filter { state -> state.isHidden }
                            .map { state -> state.conversationId }
                            .toSet()
                        val joinedConversationIds = currentUserParticipations
                            .map { it.conversationId }
                            .filterNot { conversationId -> hiddenConversationIds.contains(conversationId) }
                            .toSet()
                        if (joinedConversationIds.isEmpty()) {
                            emit(emptyList())
                            return@flow
                        }

                        val filteredBundles = bundles.filter { bundle ->
                            joinedConversationIds.contains(bundle.conversation.conversationId)
                        }
                        if (filteredBundles.isEmpty()) {
                            emit(emptyList())
                            return@flow
                        }

                        val participantsByConversationId = allParticipants
                            .filter { participant ->
                                joinedConversationIds.contains(participant.conversationId)
                            }
                            .groupBy { it.conversationId }
                        val usersById = loadUsersById(
                            participantsByConversationId.values
                                .flatten()
                                .map { it.userId }
                                .distinct()
                        )

                        emit(
                            filteredBundles.map { bundle ->
                                val conversationId = bundle.conversation.conversationId
                                val peerUsers = participantsByConversationId[conversationId]
                                    .orEmpty()
                                    .filterNot { participant -> participant.userId == currentUserId }
                                    .map { participant ->
                                        usersById[participant.userId]
                                            ?: buildPlaceholderUser(participant.userId)
                                    }
                                bundle.toDomain(peerUsers)
                            }
                        )
                    }
                }
            }
        }
        .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())

    val currentUserConversationStates: StateFlow<List<ConversationUserStateEntity>> =
        userRepository.currentUserId
            .flatMapLatest { currentUserId ->
                if (currentUserId == null) flowOf(emptyList()) else conversationUserStateDao.observeStatesByUserId(currentUserId)
            }
            .stateIn(repositoryScope, SharingStarted.Eagerly, emptyList())


    suspend fun saveConversation(conversation: Conversation) {
        conversationDao.upsertConversation(conversation.toEntity())
        syncParticipants(conversation.conversationId, buildConversationParticipantIds(conversation))
        ensureCurrentUserState(conversation.conversationId)
    }

    suspend fun syncRemoteConversation(
        conversation: Conversation,
        unreadCount: Int,
        isHidden: Boolean
    ) {
        conversationDao.upsertConversation(conversation.toEntity())
        syncParticipants(conversation.conversationId, buildConversationParticipantIds(conversation))
        val currentUserId = userRepository.currentUserId.value ?: return
        conversationUserStateDao.upsertState(
            ConversationUserStateEntity(
                conversationId = conversation.conversationId,
                userId = currentUserId,
                unreadCount = unreadCount,
                isHidden = isHidden
            )
        )
    }

    suspend fun findOrCreateSingleConversation(peerUser: User): Conversation {
        ensureUserExists(peerUser)
        conversationDao.getConversationBundleByTarget(
            conversationType = Conversation.ConversationType.SINGLE.name,
            targetId = peerUser.id
        )?.let { bundle ->
            syncParticipants(
                bundle.conversation.conversationId,
                buildConversationParticipantIds(
                    Conversation(
                        conversationId = bundle.conversation.conversationId,
                        type = Conversation.ConversationType.SINGLE,
                        targetId = peerUser.id,
                        conversationPeer = Conversation.ConversationPeer.Single(peerUser),
                        lastMessage = buildPlaceholderMessage(),
                        lastTime = bundle.conversation.lastTime
                    )
                )
            )
            ensureCurrentUserState(bundle.conversation.conversationId)
            return bundle.toDomain(listOf(peerUser))
        }

        val conversation = Conversation(
            conversationId = UUID.randomUUID().toString(),
            type = Conversation.ConversationType.SINGLE,
            targetId = peerUser.id,
            conversationPeer = Conversation.ConversationPeer.Single(peerUser),
            lastMessage = buildPlaceholderMessage(),
            lastTime = System.currentTimeMillis()
        )
        saveConversation(conversation)
        return conversation
    }

    suspend fun findOrCreateGroupConversation(groupId: Long, members: List<User>): Conversation {
        val distinctMembers = members.distinctBy { it.id }
        ensureUsersExist(distinctMembers)
        conversationDao.getConversationBundleByTarget(
            conversationType = Conversation.ConversationType.GROUP.name,
            targetId = groupId
        )?.let { bundle ->
            syncParticipants(
                bundle.conversation.conversationId,
                buildConversationParticipantIds(
                    Conversation(
                        conversationId = bundle.conversation.conversationId,
                        type = Conversation.ConversationType.GROUP,
                        targetId = groupId,
                        conversationPeer = Conversation.ConversationPeer.Group(distinctMembers),
                        lastMessage = buildPlaceholderMessage(),
                        lastTime = bundle.conversation.lastTime
                    )
                )
            )
            ensureCurrentUserState(bundle.conversation.conversationId)
            return bundle.toDomain(distinctMembers)
        }

        val conversation = Conversation(
            conversationId = UUID.randomUUID().toString(),
            type = Conversation.ConversationType.GROUP,
            targetId = groupId,
            conversationPeer = Conversation.ConversationPeer.Group(distinctMembers),
            lastMessage = buildPlaceholderMessage(),
            lastTime = System.currentTimeMillis()
        )
        saveConversation(conversation)
        return conversation
    }

    suspend fun getConversationById(conversationId: String): Conversation? {
        val bundle = conversationDao.getConversationBundleById(conversationId) ?: return null
        val participants = conversationRelationDao.getParticipants(conversationId)
        val peerUsers = buildOrderedPeerUsers(participants)
        return bundle.toDomain(peerUsers)
    }

    suspend fun getAllConversations(): List<Conversation> = allConversations.value

    suspend fun deleteConversationById(conversationId: String) {
        conversationUserStateDao.deleteStatesByConversationId(conversationId)
        conversationRelationDao.deleteAllParticipants(conversationId)
        conversationDao.deleteConversationById(conversationId)
    }

    suspend fun setConversationHidden(conversationId: String, hidden: Boolean) {
        val currentUserId = userRepository.currentUserId.value ?: return
        ensureCurrentUserState(conversationId)
        conversationUserStateDao.updateHiddenState(conversationId, currentUserId, hidden)
    }

    suspend fun markConversationAsRead(conversationId: String) {
        val currentUserId = userRepository.currentUserId.value ?: return
        ensureCurrentUserState(conversationId)
        conversationUserStateDao.updateUnreadCount(conversationId, currentUserId, 0)
    }

    private suspend fun syncParticipants(conversationId: String, userIds: List<Long>) {
        conversationRelationDao.deleteAllParticipants(conversationId)
        if (userIds.isNotEmpty()) {
            conversationRelationDao.insertParticipants(
                userIds.mapIndexed { index, userId ->
                    ConversationParticipantEntity(
                        conversationId = conversationId,
                        userId = userId,
                        sortOrder = index
                    )
                }
            )
            ensureUserStates(conversationId, userIds)
        }
    }

    private suspend fun ensureCurrentUserState(conversationId: String) {
        val currentUserId = userRepository.currentUserId.value ?: return
        conversationUserStateDao.insertStateIfAbsent(
            ConversationUserStateEntity(conversationId = conversationId, userId = currentUserId, unreadCount = 0)
        )
    }

    private suspend fun ensureUserStates(conversationId: String, userIds: List<Long>) {
        val states = userIds.distinct().map { userId ->
            ConversationUserStateEntity(conversationId = conversationId, userId = userId, unreadCount = 0)
        }
        if (states.isNotEmpty()) {
            conversationUserStateDao.insertStatesIfAbsent(states)
        }
    }

    private suspend fun buildOrderedPeerUsers(participants: List<ConversationParticipantEntity>): List<User> {
        val currentUserId = userRepository.currentUserId.value
        val usersById = loadUsersById(participants.map { it.userId })
        return participants
            .filterNot { participant -> participant.userId == currentUserId }
            .map { participant -> usersById[participant.userId] ?: buildPlaceholderUser(participant.userId) }
    }

    private suspend fun loadUsersById(userIds: List<Long>): Map<Long, User> {
        if (userIds.isEmpty()) return emptyMap()
        return userDao.getUserBasicBundlesByIds(userIds.distinct()).map { it.toDomain() }.associateBy { it.id }
    }

    private suspend fun ensureUserExists(user: User) {
        val exists = userDao.getUserBasicBundlesByIds(listOf(user.id)).isNotEmpty()
        if (!exists) userDao.saveUser(user)
    }

    private suspend fun ensureUsersExist(users: List<User>) {
        if (users.isEmpty()) return
        val distinctUsers = users.distinctBy { it.id }
        val existingIds = userDao.getUserBasicBundlesByIds(distinctUsers.map { it.id }).map { it.account.userId }.toSet()
        distinctUsers.filterNot { user -> existingIds.contains(user.id) }.forEach { user -> userDao.saveUser(user) }
    }

    private fun buildConversationParticipantIds(conversation: Conversation): List<Long> {
        val currentUserId = userRepository.currentUserId.value
        val participantIds = buildList {
            currentUserId?.let(::add)
            addAll(buildPeerIds(conversation.conversationPeer))
        }
        return participantIds.distinct()
    }

    private fun buildPlaceholderUser(userId: Long): User = User(id = userId, name = "", headPic = "")

    private fun buildPlaceholderMessage(): Message = Message(
        messageId = "",
        senderId = 0L,
        type = Message.MessageType.TEXT,
        content = "",
        timestamp = System.currentTimeMillis(),
        status = Message.MessageStatus.SENT
    )

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
