package com.example.grabthisforme.model.user.mapper

import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserBundleEntity
import com.example.grabthisforme.model.user.data.entity.UserLikeEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity
import com.example.grabthisforme.model.user.data.entity.UserStatisticsEntity
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserAccount
import com.example.grabthisforme.model.user.domain.UserProfile
import org.json.JSONArray

private fun List<String>.toJsonArrayString(): String {
    return JSONArray(this).toString()
}

private fun List<Long>.toLongJsonArrayString(): String {
    val array = JSONArray()
    forEach { array.put(it) }
    return array.toString()
}

private fun String.toStringList(): List<String> {
    if (isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.optString(index) }
            .filter { it.isNotBlank() }
    }.getOrDefault(emptyList())
}

private fun String.toLongList(): List<Long> {
    if (isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.optLong(index) }
            .filter { it > 0L }
    }.getOrDefault(emptyList())
}

fun User.toAccountEntity(): UserAccountEntity {
    return UserAccountEntity(
        userId = id,
        accountName = accountName,
        passwordHash = account.passwordHash,
        isCurrent = isCurrent,
        createTime = createTime,
        lastLoginTime = account.lastLoginTime
    )
}

fun User.toProfileEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = id,
        displayName = name,
        avatarUrl = headPic,
        phone = phone,
        email = email,
        gender = gender,
        isVip = isVip,
        signature = signature
    )
}

fun User.toStatisticsEntity(): UserStatisticsEntity {
    return UserStatisticsEntity(
        userId = id,
        likeCount = likeCount,
        fanCount = fanCount,
        followCount = followCount,
        selfPostsJson = selfPosts.toJsonArrayString()
    )
}

fun User.toLikeEntity(): UserLikeEntity {
    return UserLikeEntity(
        userId = id,
        likedPostIdsJson = likedPostIds.toJsonArrayString(),
        likedStoreIdsJson = likedStoreIds.toLongJsonArrayString(),
        likedGoodsIdsJson = likedGoodsIds.toLongJsonArrayString()
    )
}

fun UserBundleEntity.toDomain(): User {
    val profileEntity = profile
    val statisticsEntity = statistics
    val likeEntity = likes
    return User(
        id = account.userId,
        name = profileEntity?.displayName ?: account.accountName,
        headPic = profileEntity?.avatarUrl.orEmpty(),
        phone = profileEntity?.phone,
        email = profileEntity?.email,
        gender = profileEntity?.gender ?: UserProfile.GENDER_UNKNOWN,
        createTime = account.createTime,
        isVip = profileEntity?.isVip ?: false,
        signature = profileEntity?.signature,
        isCurrent = account.isCurrent,
        accountName = account.accountName,
        passwordHash = account.passwordHash,
        lastLoginTime = account.lastLoginTime,
        likeCount = statisticsEntity?.likeCount ?: 0L,
        fanCount = statisticsEntity?.fanCount ?: 0L,
        followCount = statisticsEntity?.followCount ?: 0L,
        selfPosts = statisticsEntity?.selfPostsJson?.toStringList().orEmpty(),
        likedPostIds = likeEntity?.likedPostIdsJson?.toStringList().orEmpty(),
        likedStoreIds = likeEntity?.likedStoreIdsJson?.toLongList().orEmpty(),
        likedGoodsIds = likeEntity?.likedGoodsIdsJson?.toLongList().orEmpty()
    )
}

fun UserAccountEntity.toDomain(
    profile: UserProfileEntity? = null,
    statistics: UserStatisticsEntity? = null,
    likes: UserLikeEntity? = null
): User {
    return User(
        id = userId,
        name = profile?.displayName ?: accountName,
        headPic = profile?.avatarUrl.orEmpty(),
        phone = profile?.phone,
        email = profile?.email,
        gender = profile?.gender ?: UserProfile.GENDER_UNKNOWN,
        createTime = createTime,
        isVip = profile?.isVip ?: false,
        signature = profile?.signature,
        isCurrent = isCurrent,
        accountName = accountName,
        passwordHash = passwordHash,
        lastLoginTime = lastLoginTime,
        likeCount = statistics?.likeCount ?: 0L,
        fanCount = statistics?.fanCount ?: 0L,
        followCount = statistics?.followCount ?: 0L,
        selfPosts = statistics?.selfPostsJson?.toStringList().orEmpty(),
        likedPostIds = likes?.likedPostIdsJson?.toStringList().orEmpty(),
        likedStoreIds = likes?.likedStoreIdsJson?.toLongList().orEmpty(),
        likedGoodsIds = likes?.likedGoodsIdsJson?.toLongList().orEmpty()
    )
}

