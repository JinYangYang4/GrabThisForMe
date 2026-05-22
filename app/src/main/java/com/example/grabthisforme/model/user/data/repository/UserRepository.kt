package com.example.grabthisforme.model.user.data.repository

import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserLike
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository@Inject constructor(
    private val userDao: UserDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val currentUser: StateFlow<User?> = userDao.getCurrentUser()
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

    val allLoginUsers: StateFlow<List<User>> = userDao.getAllLoginUsers()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    suspend fun upsertUser(user: User) {
        userDao.saveUser(user)
    }

    suspend fun upsertAndSetCurrent(user: User) {
        userDao.loginAndSetCurrent(user)
    }

    suspend fun updateCurrentUserLikes(transform: (UserLike) -> UserLike): User? {
        val user = currentUser.value ?: return null
        val updatedLikes = transform(user.likes)
        val updatedUser = user.withLikes(
            likedPostIds = updatedLikes.likedPostIds,
            likedStoreIds = updatedLikes.likedStoreIds,
            likedGoodsIds = updatedLikes.likedGoodsIds
        )
        userDao.saveUser(updatedUser)
        return updatedUser
    }

    suspend fun setPostLiked(postId: String, liked: Boolean): User? {
        if (postId.isBlank()) return currentUser.value
        return updateCurrentUserLikes { likes ->
            val updatedIds = likes.likedPostIds.toMutableList().apply {
                if (liked) {
                    if (!contains(postId)) add(postId)
                } else {
                    remove(postId)
                }
            }
            likes.copy(likedPostIds = updatedIds)
        }
    }

    suspend fun setStoreLiked(storeId: Long, liked: Boolean): User? {
        if (storeId <= 0L) return currentUser.value
        return updateCurrentUserLikes { likes ->
            val updatedIds = likes.likedStoreIds.toMutableList().apply {
                if (liked) {
                    if (!contains(storeId)) add(storeId)
                } else {
                    remove(storeId)
                }
            }
            likes.copy(likedStoreIds = updatedIds)
        }
    }

    suspend fun setGoodsLiked(goodsId: Long, liked: Boolean): User? {
        if (goodsId <= 0L) return currentUser.value
        return updateCurrentUserLikes { likes ->
            val updatedIds = likes.likedGoodsIds.toMutableList().apply {
                if (liked) {
                    if (!contains(goodsId)) add(goodsId)
                } else {
                    remove(goodsId)
                }
            }
            likes.copy(likedGoodsIds = updatedIds)
        }
    }
}
