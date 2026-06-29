package com.example.grabthisforme.model.user.data.repository

import com.example.grabthisforme.model.goods.data.network.dto.GoodsDto
import com.example.grabthisforme.model.post.data.network.dto.PostDto
import com.example.grabthisforme.model.store.data.network.dto.StoreDto
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class UserRepository @Inject constructor(
    private val localRepository: UserLocalRepository,
    private val remoteRepository: UserRemoteRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUser: StateFlow<User?> = localRepository.currentUser
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val currentUserId: StateFlow<Long?> = currentUser
        .map { it?.id }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val allLoginUsers: StateFlow<List<User>> = localRepository.allLoginUsers
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    suspend fun upsertUser(user: User) {
        localRepository.saveUser(user)
    }

    suspend fun upsertUsers(users: List<User>) {
        localRepository.saveUsers(users)
    }

    suspend fun ensureCachedUsers(users: List<User>) {
        localRepository.ensureCachedUsers(users)
    }

    suspend fun upsertAndSetCurrent(user: User) {
        localRepository.setCurrentUser(user)
    }

    suspend fun logoutCurrentUser() {
        localRepository.logoutCurrentUser()
    }

    suspend fun deleteUserById(userId: Long) {
        localRepository.deleteUserById(userId)
    }

    suspend fun deleteUsersByIds(userIds: List<Long>) {
        localRepository.deleteUsersByIds(userIds)
    }

    suspend fun updateCurrentUserStatistics(transform: (UserStatistics) -> UserStatistics): User? {
        return localRepository.updateCurrentUserStatistics(transform)
    }

    fun isStoreLiked(storeId: Long): Flow<Boolean> {
        return currentUserId.flatMapLatest { userId ->
            if (userId == null || storeId <= 0L) {
                flowOf(false)
            } else {
                localRepository.isStoreLikedFlow(userId, storeId)
            }
        }
    }

    suspend fun setStoreLiked(storeId: Long, liked: Boolean): Boolean {
        val userId = currentUserId.value ?: return false
        if (storeId <= 0L) return false
        return localRepository.setStoreLiked(userId, storeId, liked)
    }

    fun isGoodsLiked(goodsId: Long): Flow<Boolean> {
        return currentUserId.flatMapLatest { userId ->
            if (userId == null || goodsId <= 0L) {
                flowOf(false)
            } else {
                localRepository.isGoodsLikedFlow(userId, goodsId)
            }
        }
    }

    suspend fun setGoodsLiked(goodsId: Long, liked: Boolean): Boolean {
        val userId = currentUserId.value ?: return false
        if (goodsId <= 0L) return false
        return localRepository.setGoodsLiked(userId, goodsId, liked)
    }

    suspend fun getUserPosts(userId: Long): Result<List<PostDto>> {
        return remoteRepository.getUserPosts(userId)
            .recoverCatching {
                emptyList()
            }
    }

    suspend fun getLikedPosts(userId: Long): Result<List<PostDto>> {
        return remoteRepository.getLikedPosts(userId)
            .recoverCatching {
                emptyList()
            }
    }

    suspend fun getLikedStores(userId: Long): Result<List<StoreDto>> {
        return remoteRepository.getLikedStores(userId)
            .recoverCatching {
                emptyList()
            }
    }

    suspend fun getLikedGoods(userId: Long): Result<List<GoodsDto>> {
        return remoteRepository.getLikedGoods(userId)
            .recoverCatching {
                emptyList()
            }
    }
}
