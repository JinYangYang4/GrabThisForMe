package com.example.grabthisforme.model.user.domain

import com.example.grabthisforme.model.user.data.mock.UserSampleData

data class User(
    val account: UserAccount,
    val profile: UserProfile,
    val setting: UserSetting? = null,
    val statistics: UserStatistics = UserStatistics(),
    val likes: UserLike = UserLike()
) {
    val id: Long get() = account.userId
    val accountName: String get() = account.accountName
    val isCurrent: Boolean get() = account.isCurrent
    val name: String get() = profile.displayName
    val headPic: String get() = profile.avatarUrl
    val phone: String? get() = profile.phone
    val email: String? get() = profile.email
    val gender: Int get() = profile.gender
    val createTime: Long get() = account.createTime
    val isVip: Boolean get() = profile.isVip
    val signature: String? get() = profile.signature
    val likeCount: Long get() = statistics.likeCount
    val fanCount: Long get() = statistics.fanCount
    val followCount: Long get() = statistics.followCount
    val likedPostIds: List<String> get() = likes.likedPostIds
    val likedStoreIds: List<Long> get() = likes.likedStoreIds
    val likedGoodsIds: List<Long> get() = likes.likedGoodsIds

    constructor(
        id: Long,
        name: String,
        headPic: String,
        phone: String? = null,
        email: String? = null,
        gender: Int = UserProfile.GENDER_UNKNOWN,
        createTime: Long = System.currentTimeMillis(),
        isVip: Boolean = false,
        signature: String? = null,
        isCurrent: Boolean = false,
        accountName: String = name,
        passwordHash: String = "",
        lastLoginTime: Long? = null,
        setting: UserSetting? = null,
        likeCount: Long = 0L,
        fanCount: Long = 0L,
        followCount: Long = 0L,
        likedPostIds: List<String> = emptyList(),
        likedStoreIds: List<Long> = emptyList(),
        likedGoodsIds: List<Long> = emptyList()
    ) : this(
        account = UserAccount(
            userId = id,
            accountName = accountName,
            passwordHash = passwordHash,
            isCurrent = isCurrent,
            createTime = createTime,
            lastLoginTime = lastLoginTime
        ),
        profile = UserProfile(
            userId = id,
            displayName = name,
            avatarUrl = headPic,
            phone = phone,
            email = email,
            gender = gender,
            isVip = isVip,
            signature = signature
        ),
        setting = setting,
        statistics = UserStatistics(
            likeCount = likeCount,
            fanCount = fanCount,
            followCount = followCount
        ),
        likes = UserLike(
            likedPostIds = likedPostIds,
            likedStoreIds = likedStoreIds,
            likedGoodsIds = likedGoodsIds
        )
    )

    fun getInfoSummary(): String {
        return "User ID: $id, name: $name, vip: ${if (isVip) "true" else "false"}"
    }

    fun isBasicInfoComplete(): Boolean {
        return name.isNotBlank() && headPic.isNotBlank()
    }

    fun withCurrent(isCurrent: Boolean): User {
        return copy(
            account = account.copy(isCurrent = isCurrent)
        )
    }

    fun withStatistics(
        likeCount: Long = statistics.likeCount,
        fanCount: Long = statistics.fanCount,
        followCount: Long = statistics.followCount
    ): User {
        return copy(
            statistics = statistics.copy(
                likeCount = likeCount,
                fanCount = fanCount,
                followCount = followCount
            )
        )
    }

    fun withLikes(
        likedPostIds: List<String> = likes.likedPostIds,
        likedStoreIds: List<Long> = likes.likedStoreIds,
        likedGoodsIds: List<Long> = likes.likedGoodsIds
    ): User {
        return copy(
            likes = likes.copy(
                likedPostIds = likedPostIds,
                likedStoreIds = likedStoreIds,
                likedGoodsIds = likedGoodsIds
            )
        )
    }

    companion object {
        fun createVirtualUsers(
            templateUser: User,
            count: Int,
            randomName: Boolean = true,
            randomVip: Boolean = false
        ): List<User> = UserSampleData.createVirtualUsers(
            templateUser = templateUser,
            count = count,
            randomName = randomName,
            randomVip = randomVip
        )

        fun getVirtualUser(): User = UserSampleData.getVirtualUser()
    }
}
