package com.example.grabthisforme.model.user.data.repository

import android.util.Log
import com.example.grabthisforme.model.relation.data.dao.UserRelationDao
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.user.data.local.dao.UserDao
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserStatistics
import com.example.grabthisforme.model.user.mapper.toDomain
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
    companion object {
        private const val TAG = "UserCacheDiag"
    }

    val currentUser: Flow<User?> = userDao.getCurrentUser()

    val allLoginUsers: Flow<List<User>> = userDao.getAllLoginUsers()

    suspend fun saveUser(user: User) {
        userDao.saveUser(resolvePersistedUser(user))
    }

    suspend fun saveUsers(users: List<User>) {
        users.forEach { userDao.saveUser(resolvePersistedUser(it)) }
    }

    suspend fun ensureCachedUsers(users: List<User>) {
        if (users.isEmpty()) return
        val distinctUsers = users
            .filter { user -> user.id > 0L }
            .distinctBy { user -> user.id }
            .map(::toCachedUser)
        if (distinctUsers.isEmpty()) return

        val existingIds = userDao.getUserBasicBundlesByIds(distinctUsers.map { it.id })
            .map { it.account.userId }
            .toSet()
        Log.d(
            TAG,
            "ensureCachedUsers start: requested=${distinctUsers.map { it.id }}, existing=${existingIds.toList()}"
        )

        distinctUsers
            .filterNot { user -> existingIds.contains(user.id) }
            .forEach { user ->
                val resolved = resolvePersistedUser(user)
                Log.d(
                    TAG,
                    "cache user insert: userId=${resolved.id}, accountName=${resolved.accountName}, isLoginAccount=${resolved.isLoginAccount}"
                )
                userDao.saveUser(resolved)
            }
    }

    suspend fun setCurrentUser(user: User) {
        val resolvedUser = resolvePersistedUser(user)
        userDao.loginAndSetCurrent(
            resolvedUser.copy(
                account = resolvedUser.account.copy(
                    isCurrent = true,
                    isLoginAccount = true
                )
            )
        )
    }

    suspend fun logoutCurrentUser() {
        userDao.logoutCurrentUser()
    }

    suspend fun deleteUserById(userId: Long) {
        userDao.clearLoginAccount(userId)
    }

    suspend fun deleteUsersByIds(userIds: List<Long>) {
        userDao.clearLoginAccounts(userIds)
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

    private suspend fun resolvePersistedUser(user: User): User {
        val existing = userDao.getUserBasicBundlesByIds(listOf(user.id))
            .firstOrNull()
            ?.toDomain()
        val resolvedPassword = user.account.passwordHash.ifBlank {
            existing?.account?.passwordHash.orEmpty()
        }
        val resolvedIsLoginAccount = user.isLoginAccount || existing?.isLoginAccount == true
        val resolvedIsCurrent = user.isCurrent || existing?.isCurrent == true
        return user.copy(
            account = user.account.copy(
                passwordHash = resolvedPassword,
                isLoginAccount = resolvedIsLoginAccount,
                isCurrent = resolvedIsCurrent
            )
        )
    }

    private fun toCachedUser(user: User): User {
        return user.copy(
            account = user.account.copy(
                accountName = user.account.accountName.ifBlank { user.id.toString() },
                passwordHash = "",
                isCurrent = false,
                isLoginAccount = false
            ),
            profile = user.profile.copy(
                displayName = user.profile.displayName.ifBlank { user.account.accountName.ifBlank { user.id.toString() } },
                avatarUrl = user.profile.avatarUrl
            )
        )
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
