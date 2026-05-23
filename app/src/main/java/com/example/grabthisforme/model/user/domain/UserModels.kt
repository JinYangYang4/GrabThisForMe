package com.example.grabthisforme.model.user.domain

data class UserAccount(
    val userId: Long,
    val accountName: String,
    val passwordHash: String = "",
    val isCurrent: Boolean = false,
    val createTime: Long = System.currentTimeMillis(),
    val lastLoginTime: Long? = null
)

data class UserProfile(
    val userId: Long,
    val displayName: String,
    val avatarUrl: String = "",
    val phone: String? = null,
    val email: String? = null,
    val gender: Int = GENDER_UNKNOWN,
    val isVip: Boolean = false,
    val signature: String? = null
) {
    companion object {
        const val GENDER_UNKNOWN = 0
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 2
    }
}

data class UserStatistics(
    val likeCount: Long = 0L,
    val fanCount: Long = 0L,
    val followCount: Long = 0L,
    val selfPosts: List<String> = emptyList()
)

data class UserLike(
    val likedPostIds: List<String> = emptyList(),
    val likedStoreIds: List<Long> = emptyList(),
    val likedGoodsIds: List<Long> = emptyList()
) {
    fun hasLikedPost(postId: String): Boolean = likedPostIds.contains(postId)

    fun hasLikedStore(storeId: Long): Boolean = likedStoreIds.contains(storeId)

    fun hasLikedGoods(goodsId: Long): Boolean = likedGoodsIds.contains(goodsId)
}

data class UserSetting(
    val themeMode: Int = 0,
    val homePageMode: Int = 0,
    val chatBackground: String = "",
    val receiveNotification: Boolean = true,
    val lastBottomTab: Int = 0
)
