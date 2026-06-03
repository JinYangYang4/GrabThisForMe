package com.example.grabthisforme.model.user.data.repository

import com.example.grabthisforme.model.relation.data.dao.UserRelationDao
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class UserRepository@Inject constructor(
    private val userDao: UserDao,
    private val userRelationDao: UserRelationDao
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

    suspend fun updateCurrentUserStatistics(transform: (UserStatistics) -> UserStatistics): User? {
        val user = currentUser.value ?: return null
        val updatedStatistics = transform(user.statistics)
        val updatedUser = user.withStatistics(
            likeCount = updatedStatistics.likeCount,
            fanCount = updatedStatistics.fanCount,
            followCount = updatedStatistics.followCount,
            selfPosts = updatedStatistics.selfPosts
        )
        userDao.saveUser(updatedUser)
        return updatedUser
    }

    fun isStoreLiked(storeId: Long): Flow<Boolean> {
        return currentUserId.flatMapLatest { userId ->
            if (userId == null || storeId <= 0L) {
                flowOf(false)
            } else {
                userRelationDao.isStoreLikedFlow(userId, storeId)
            }
        }
    }

    suspend fun setStoreLiked(storeId: Long, liked: Boolean): Boolean {
        val userId = currentUserId.value ?: return false
        if (storeId <= 0L) return false
        val currentlyLiked = userRelationDao.isStoreLiked(userId, storeId)
        if (currentlyLiked == liked) return liked
        if (liked) {
            userRelationDao.insertLikedStore(
                UserLikedStoreEntity(
                    userId = userId,
                    storeId = storeId
                )
            )
        } else {
            userRelationDao.deleteLikedStore(userId, storeId)
        }
        return liked
    }

    fun isGoodsLiked(goodsId: Long): Flow<Boolean> {
        return currentUserId.flatMapLatest { userId ->
            if (userId == null || goodsId <= 0L) {
                flowOf(false)
            } else {
                userRelationDao.isGoodsLikedFlow(userId, goodsId)
            }
        }
    }

    suspend fun setGoodsLiked(goodsId: Long, liked: Boolean): Boolean {
        val userId = currentUserId.value ?: return false
        if (goodsId <= 0L) return false
        val currentlyLiked = userRelationDao.isGoodsLiked(userId, goodsId)
        if (currentlyLiked == liked) return liked
        if (liked) {
            userRelationDao.insertLikedGoods(
                UserLikedGoodsEntity(
                    userId = userId,
                    goodsId = goodsId
                )
            )
        } else {
            userRelationDao.deleteLikedGoods(userId, goodsId)
        }
        return liked
    }
}
