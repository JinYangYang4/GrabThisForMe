package com.example.grabthisforme.model.user.mapper

import com.example.grabthisforme.model.user.data.local.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.local.entity.UserBasicBundleEntity
import com.example.grabthisforme.model.user.data.local.entity.UserBundleEntity
import com.example.grabthisforme.model.user.data.local.entity.UserProfileEntity
import com.example.grabthisforme.model.user.data.local.entity.UserStatisticsEntity
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import com.example.grabthisforme.model.user.data.network.dto.UserStatisticsDto
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserAccount
import com.example.grabthisforme.model.user.domain.UserProfile

fun User.toAccountEntity(): UserAccountEntity {
    return UserAccountEntity(
        userId = id,
        accountName = accountName,
        passwordHash = account.passwordHash,
        isCurrent = isCurrent,
        isLoginAccount = account.isLoginAccount,
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
        followCount = followCount
    )
}

fun UserDto.toDomain(
    passwordHash: String = "",
    isLoginAccount: Boolean = false
): User {
    return User(
        id = id,
        name = name ?: accountName ?: id.toString(),
        headPic = headPic.orEmpty(),
        phone = phone,
        email = email,
        gender = gender ?: UserProfile.GENDER_UNKNOWN,
        createTime = createTime ?: System.currentTimeMillis(),
        isVip = isVip ?: false,
        signature = signature,
        accountName = accountName ?: id.toString(),
        passwordHash = passwordHash,
        isLoginAccount = isLoginAccount,
        lastLoginTime = lastLoginTime,
        likeCount = statistics?.likeCount ?: 0L,
        fanCount = statistics?.fanCount ?: 0L,
        followCount = statistics?.followCount ?: 0L,
        selfPosts = emptyList(),
        likedPostIds = emptyList(),
        likedStoreIds = emptyList(),
        likedGoodsIds = emptyList()
    )
}

fun User.toDto(): UserDto {
    return UserDto(
        id = id,
        accountName = accountName,
        name = name,
        headPic = headPic,
        phone = phone,
        email = email,
        gender = gender,
        isVip = isVip,
        signature = signature,
        createTime = createTime,
        lastLoginTime = account.lastLoginTime,
        statistics = UserStatisticsDto(
            likeCount = likeCount,
            fanCount = fanCount,
            followCount = followCount
        )
    )
}

fun UserBundleEntity.toDomain(): User {
    val profileEntity = profile
    val statisticsEntity = statistics
    val selfPostIds = userPosts
        .sortedByDescending { it.postId }
        .map { it.postId }
    val likedPostIds = likedPosts
        .sortedByDescending { it.likedAt }
        .map { it.postId }
    val likedStoreIds = likedStores
        .sortedByDescending { it.likedAt }
        .map { it.storeId }
    val likedGoodsIds = likedGoods
        .sortedByDescending { it.likedAt }
        .map { it.goodsId }
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
        isLoginAccount = account.isLoginAccount,
        lastLoginTime = account.lastLoginTime,
        likeCount = statisticsEntity?.likeCount ?: 0L,
        fanCount = statisticsEntity?.fanCount ?: 0L,
        followCount = statisticsEntity?.followCount ?: 0L,
        selfPosts = selfPostIds,
        likedPostIds = likedPostIds,
        likedStoreIds = likedStoreIds,
        likedGoodsIds = likedGoodsIds
    )
}

fun UserBasicBundleEntity.toDomain(): User {
    return account.toDomain(profile, statistics)
}

fun UserAccountEntity.toDomain(
    profile: UserProfileEntity? = null,
    statistics: UserStatisticsEntity? = null
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
        isLoginAccount = isLoginAccount,
        lastLoginTime = lastLoginTime,
        likeCount = statistics?.likeCount ?: 0L,
        fanCount = statistics?.fanCount ?: 0L,
        followCount = statistics?.followCount ?: 0L
    )
}
