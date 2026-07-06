package com.example.grabthisforme.model.user.data.network.dto

data class UserBriefDto(
    val id: Long,
    val accountName: String? = null,
    val name: String? = null,
    val headPic: String? = null
)
