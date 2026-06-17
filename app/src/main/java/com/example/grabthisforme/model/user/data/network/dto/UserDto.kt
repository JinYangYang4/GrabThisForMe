package com.example.grabthisforme.model.user.data.network.dto

data class UserDto(
    val id: Long,
    val accountName: String? = null,
    val name: String? = null,
    val headPic: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val gender: Int? = null,
    val isVip: Boolean? = null,
    val signature: String? = null,
    val createTime: Long? = null,
    val lastLoginTime: Long? = null,
    val statistics: UserStatisticsDto? = null
)

data class UserStatisticsDto(
    val likeCount: Long? = null,
    val fanCount: Long? = null,
    val followCount: Long? = null
)
