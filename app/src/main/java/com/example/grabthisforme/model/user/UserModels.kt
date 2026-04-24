package com.example.grabthisforme.model.user

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

data class UserSetting(
    val themeMode: Int = 0,
    val homePageMode: Int = 0,
    val chatBackground: String = "",
    val receiveNotification: Boolean = true,
    val lastBottomTab: Int = 0
)

