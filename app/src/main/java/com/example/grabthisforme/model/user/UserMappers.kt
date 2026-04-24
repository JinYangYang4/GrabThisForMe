package com.example.grabthisforme.model.user


//数据转换
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

