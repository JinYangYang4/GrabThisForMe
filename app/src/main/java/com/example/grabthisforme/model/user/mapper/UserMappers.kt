package com.example.grabthisforme.model.user.mapper

import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserBundleEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserAccount
import com.example.grabthisforme.model.user.domain.UserProfile

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

fun UserBundleEntity.toDomain(): User {
    val profileEntity = profile
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
        lastLoginTime = account.lastLoginTime
    )
}

fun UserAccountEntity.toDomain(profile: UserProfileEntity? = null): User {
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
        lastLoginTime = lastLoginTime
    )
}

