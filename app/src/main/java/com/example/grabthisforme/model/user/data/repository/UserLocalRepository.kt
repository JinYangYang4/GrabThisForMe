package com.example.grabthisforme.model.user.data.repository

import com.example.grabthisforme.model.relation.data.dao.UserRelationDao
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.user.data.local.dao.UserDao
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserStatistics
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class UserLocalRepository @Inject constructor(
    private val userDao: UserDao,
    private val userRelationDao: UserRelationDao
) {

    val currentUser: Flow<User?> = userDao.getCurrentUser()

    val allLoginUsers: Flow<List<User>> = userDao.getAllLoginUsers()

    suspend fun saveUser(user: User) {
        userDao.saveUser(user)
    }

    suspend fun saveUsers(users: List<User>) {
        users.forEach { userDao.saveUser(it) }
    }

    suspend fun setCurrentUser(user: User) {
        userDao.loginAndSetCurrent(user)
    }

    suspend fun logoutCurrentUser() {
        userDao.logoutCurrentUser()
    }

    suspend fun deleteUserById(userId: Long) {
        userDao.deleteUserById(userId)
    }

    suspend fun deleteUsersByIds(userIds: List<Long>) {
        userDao.deleteUsersByIds(userIds)
    }

    suspend fun updateCurrentUserStatistics(transform: (UserStatistics) -> UserStatistics): User? {
        val user = userDao.getCurrentUser().first() ?: return null
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

    suspend fun isStoreLiked(storeId: Long, userId: Long): Boolean {
        return userRelationDao.isStoreLiked(userId, storeId)
    }

    fun isStoreLikedFlow(userId: Long, storeId: Long): Flow<Boolean> {
        return userRelationDao.isStoreLikedFlow(userId, storeId)
    }

    suspend fun setStoreLiked(userId: Long, storeId: Long, liked: Boolean): Boolean {
        val currentlyLiked = userRelationDao.isStoreLiked(userId, storeId)
        if (currentlyLiked == liked) return liked
        if (liked) {
            userRelationDao.insertLikedStore(UserLikedStoreEntity(userId = userId, storeId = storeId))
        } else {
            userRelationDao.deleteLikedStore(userId, storeId)
        }
        return liked
    }

    suspend fun isGoodsLiked(goodsId: Long, userId: Long): Boolean {
        return userRelationDao.isGoodsLiked(userId, goodsId)
    }

    fun isGoodsLikedFlow(userId: Long, goodsId: Long): Flow<Boolean> {
        return userRelationDao.isGoodsLikedFlow(userId, goodsId)
    }

    suspend fun setGoodsLiked(userId: Long, goodsId: Long, liked: Boolean): Boolean {
        val currentlyLiked = userRelationDao.isGoodsLiked(userId, goodsId)
        if (currentlyLiked == liked) return liked
        if (liked) {
            userRelationDao.insertLikedGoods(UserLikedGoodsEntity(userId = userId, goodsId = goodsId))
        } else {
            userRelationDao.deleteLikedGoods(userId, goodsId)
        }
        return liked
    }

    suspend fun setPostLiked(userId: Long, postId: String, liked: Boolean): Boolean {
        val currentlyLiked = userRelationDao.isPostLiked(userId, postId)
        if (currentlyLiked == liked) return liked
        if (liked) {
            userRelationDao.insertLikedPost(UserLikedPostEntity(userId = userId, postId = postId))
        } else {
            userRelationDao.deleteLikedPost(userId, postId)
        }
        return liked
    }

    suspend fun getLikedPostIds(userId: Long): List<String> {
        return userRelationDao.getLikedPostsByUserId(userId).map { it.postId }
    }

    suspend fun getLikedStoreIds(userId: Long): List<Long> {
        return userRelationDao.getLikedStoresByUserId(userId).map { it.storeId }
    }

    suspend fun getLikedGoodsIds(userId: Long): List<Long> {
        return userRelationDao.getLikedGoodsByUserId(userId).map { it.goodsId }
    }

    suspend fun getUserPostIds(userId: Long): List<String> {
        return userRelationDao.getUserPostsByUserId(userId).map { it.postId }
    }
}
