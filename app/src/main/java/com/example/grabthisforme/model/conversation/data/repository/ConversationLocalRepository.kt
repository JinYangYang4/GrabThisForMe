package com.example.grabthisforme.model.conversation.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.grabthisforme.model.AppDataBase.AppDatabase
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationDao
import com.example.grabthisforme.model.conversation.data.local.dao.ConversationUserStateDao
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.conversation.mapper.buildPeerIds
import com.example.grabthisforme.model.conversation.mapper.toDomain
import com.example.grabthisforme.model.conversation.mapper.toEntity
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class ConversationLocalRepository @Inject constructor(
    private val database: AppDatabase,
    private val conversationDao: ConversationDao,
    private val conversationUserStateDao: ConversationUserStateDao,
    private val conversationRelationDao: ConversationRelationDao,
    private val userDao: UserDao,
    private val userRepository: UserRepository
) {
    companion object {
        private const val TAG = "ConversationCacheDiag"
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val conversationSyncMutex = Mutex()

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
        conversationSyncMutex.withLock {
            database.withTransaction {
                upsertConversationGraph(conversation)
                ensureCurrentUserState(conversation.conversationId)
            }
        }
    }

    suspend fun syncRemoteConversation(
        conversation: Conversation,
        unreadCount: Int,
        isHidden: Boolean,
        lastReadTime: Long? = null
    ) {
        conversationSyncMutex.withLock {
            database.withTransaction {
                Log.d(
                    TAG,
                    "syncRemoteConversation start: conversationId=${conversation.conversationId}, type=${conversation.type}, targetId=${conversation.targetId}, unreadCount=$unreadCount, isHidden=$isHidden"
                )
                upsertConversationGraph(conversation)
                val currentUserId = userRepository.currentUserId.value ?: return@withTransaction
                Log.d(
                    TAG,
                    "upsert conversation_user_state: conversationId=${conversation.conversationId}, userId=$currentUserId, unreadCount=$unreadCount, isHidden=$isHidden, lastReadTime=$lastReadTime"
                )
                conversationUserStateDao.upsertState(
                    ConversationUserStateEntity(
                        conversationId = conversation.conversationId,
                        userId = currentUserId,
                        unreadCount = unreadCount,
                        isHidden = isHidden,
                        lastReadTime = lastReadTime
                    )
                )
            }
        }
    }

    suspend fun findOrCreateSingleConversation(peerUser: User): Conversation {
        ensureUserExists(peerUser)
        findSingleConversationByPeerId(peerUser.id)?.let { existing ->
            prepareConversationForOpen(existing.conversationId)
            return existing
        }

        val conversation = Conversation(
            conversationId = UUID.randomUUID().toString(),
            type = Conversation.ConversationType.SINGLE,
            targetId = peerUser.id,
            conversationPeer = Conversation.ConversationPeer.Single(peerUser),
            lastMessage = null,
            lastTime = 0L
        )
        saveConversation(conversation)
        return conversation
    }

    suspend fun findSingleConversationByPeerId(peerUserId: Long): Conversation? {
        val bundle = conversationDao.getConversationBundleByTarget(
            conversationType = Conversation.ConversationType.SINGLE.name,
            targetId = peerUserId
        ) ?: return null
        val participants = conversationRelationDao.getParticipants(bundle.conversation.conversationId)
        val peerUsers = buildOrderedPeerUsers(participants)
        return bundle.toDomain(
            peerUsers = peerUsers.ifEmpty { listOf(buildPlaceholderUser(peerUserId)) }
        )
    }

    suspend fun findOrCreateGroupConversation(groupId: Long, members: List<User>): Conversation {
        val distinctMembers = members.distinctBy { it.id }
        ensureUsersExist(distinctMembers)
        conversationDao.getConversationBundleByTarget(
            conversationType = Conversation.ConversationType.GROUP.name,
            targetId = groupId
        )?.let { bundle ->
            prepareConversationForOpen(bundle.conversation.conversationId)
            return bundle.toDomain(distinctMembers)
        }

        val conversation = Conversation(
            conversationId = UUID.randomUUID().toString(),
            type = Conversation.ConversationType.GROUP,
            targetId = groupId,
            conversationPeer = Conversation.ConversationPeer.Group(distinctMembers),
            lastMessage = null,
            lastTime = 0L
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

    suspend fun getCurrentUserConversationState(conversationId: String): ConversationUserStateEntity? {
        val currentUserId = userRepository.currentUserId.value ?: return null
        return conversationUserStateDao.getState(conversationId, currentUserId)
    }

    suspend fun getAllConversations(): List<Conversation> = allConversations.value

    suspend fun deleteConversationById(conversationId: String) {
        conversationSyncMutex.withLock {
            database.withTransaction {
                deleteConversationDirect(conversationId)
            }
        }
    }

    suspend fun setConversationHidden(conversationId: String, hidden: Boolean) {
        val currentUserId = userRepository.currentUserId.value ?: return
        ensureCurrentUserState(conversationId)
        conversationUserStateDao.updateHiddenState(conversationId, currentUserId, hidden)
    }

    suspend fun markConversationAsRead(conversationId: String, lastReadTime: Long?) {
        val currentUserId = userRepository.currentUserId.value ?: return
        ensureCurrentUserState(conversationId)
        conversationUserStateDao.markRead(conversationId, currentUserId, lastReadTime)
    }

    suspend fun syncCurrentUserConversationMembership(
        remoteConversationIds: Set<String>,
        remoteStatesByConversationId: Map<String, ConversationUserStateEntity>
    ) {
        conversationSyncMutex.withLock {
            database.withTransaction {
                if (remoteStatesByConversationId.isNotEmpty()) {
                    conversationUserStateDao.upsertStates(remoteStatesByConversationId.values.toList())
                }
            }
        }
    }

    private suspend fun upsertConversationGraph(conversation: Conversation) {
        reconcileConversationIdentityConflict(conversation)
        Log.d(
            TAG,
            "upsert conversation: conversationId=${conversation.conversationId}, type=${conversation.type.name}, targetId=${conversation.targetId}, peerIds=${buildConversationParticipantIds(conversation.conversationPeer)}"
        )
        conversationDao.upsertConversation(conversation.toEntity())
        syncParticipants(
            conversationId = conversation.conversationId,
            userIds = buildConversationParticipantIds(conversation.conversationPeer)
        )
    }

    private suspend fun reconcileConversationIdentityConflict(conversation: Conversation) {
        val targetId = conversation.targetId ?: return
        val existing = conversationDao.getConversationBundleByTarget(
            conversationType = conversation.type.name,
            targetId = targetId
        ) ?: return
        if (existing.conversation.conversationId == conversation.conversationId) return
        Log.w(
            TAG,
            "reconcileConversationIdentityConflict delete old conversation: newConversationId=${conversation.conversationId}, oldConversationId=${existing.conversation.conversationId}, type=${conversation.type.name}, targetId=$targetId"
        )
        deleteConversationDirect(existing.conversation.conversationId)
    }

    private suspend fun deleteConversationDirect(conversationId: String) {
        conversationUserStateDao.deleteStatesByConversationId(conversationId)
        conversationRelationDao.deleteAllParticipants(conversationId)
        conversationDao.deleteConversationById(conversationId)
    }

    private suspend fun prepareConversationForOpen(conversationId: String) {
        conversationSyncMutex.withLock {
            database.withTransaction {
                val conversationExists = conversationDao.getConversationBundleById(conversationId) != null
                if (!conversationExists) {
                    Log.w(TAG, "prepareConversationForOpen skipped: missing conversationId=$conversationId")
                    return@withTransaction
                }
                ensureCurrentUserState(conversationId)
                val currentUserId = userRepository.currentUserId.value ?: return@withTransaction
                Log.d(
                    TAG,
                    "prepareConversationForOpen: conversationId=$conversationId, userId=$currentUserId, hidden=false"
                )
                conversationUserStateDao.updateHiddenState(
                    conversationId = conversationId,
                    userId = currentUserId,
                    isHidden = false
                )
            }
        }
    }

    private suspend fun syncParticipants(conversationId: String, userIds: List<Long>) {
        ensureUsersExistByIds(userIds)
        Log.d(
            TAG,
            "syncParticipants start: conversationId=$conversationId, userIds=${userIds.distinct()}"
        )
        val targetParticipants = userIds.distinct().mapIndexed { index, userId ->
            ConversationParticipantEntity(
                conversationId = conversationId,
                userId = userId,
                sortOrder = index
            )
        }
        val existingParticipants = conversationRelationDao.getParticipants(conversationId)
        val existingByUserId = existingParticipants.associateBy { it.userId }
        val targetByUserId = targetParticipants.associateBy { it.userId }

        val toDelete = existingByUserId.keys - targetByUserId.keys
        if (toDelete.isNotEmpty()) {
            Log.d(TAG, "delete conversation_participant: conversationId=$conversationId, userIds=$toDelete")
            conversationRelationDao.deleteParticipants(conversationId, toDelete.toList())
        }

        val toInsert = targetParticipants.filter { participant ->
            !existingByUserId.containsKey(participant.userId)
        }
        if (toInsert.isNotEmpty()) {
            Log.d(
                TAG,
                "insert conversation_participant: conversationId=$conversationId, userIds=${toInsert.map { it.userId }}"
            )
            conversationRelationDao.insertParticipants(toInsert)
        }

        val toUpdate = targetParticipants.filter { participant ->
            val existing = existingByUserId[participant.userId] ?: return@filter false
            existing.sortOrder != participant.sortOrder
        }
        if (toUpdate.isNotEmpty()) {
            Log.d(
                TAG,
                "update conversation_participant sortOrder: conversationId=$conversationId, userIds=${toUpdate.map { it.userId }}"
            )
            conversationRelationDao.updateParticipants(toUpdate)
        }
    }

    private suspend fun ensureCurrentUserState(conversationId: String) {
        val currentUserId = userRepository.currentUserId.value ?: return
        Log.d(
            TAG,
            "ensureCurrentUserState: conversationId=$conversationId, userId=$currentUserId"
        )
        conversationUserStateDao.insertStateIfAbsent(
            ConversationUserStateEntity(
                conversationId = conversationId,
                userId = currentUserId,
                unreadCount = 0
            )
        )
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
        return userDao.getUserBasicBundlesByIds(userIds.distinct())
            .map { it.toDomain() }
            .associateBy { it.id }
    }

    private suspend fun ensureUserExists(user: User) {
        userRepository.ensureCachedUsers(listOf(user))
    }

    private suspend fun ensureUsersExist(users: List<User>) {
        userRepository.ensureCachedUsers(users)
    }

    private suspend fun ensureUsersExistByIds(userIds: List<Long>) {
        userRepository.ensureCachedUsers(
            userIds
                .filter { it > 0L }
                .distinct()
                .map(::buildPlaceholderUser)
        )
    }

    private fun buildConversationParticipantIds(conversationPeer: Conversation.ConversationPeer): List<Long> {
        val currentUserId = userRepository.currentUserId.value
        val participantIds = buildList {
            currentUserId?.let(::add)
            addAll(buildPeerIds(conversationPeer))
        }
        return participantIds.distinct()
    }

    private fun buildPlaceholderUser(userId: Long): User = User(
        id = userId,
        name = "",
        headPic = "",
        accountName = userId.toString(),
        isLoginAccount = false
    )

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
