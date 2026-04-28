package com.example.grabthisforme.model.post.domain

import com.example.grabthisforme.model.user.domain.User

data class PostIdentity(
    val postId: String,
    val createTime: Long
)

data class PostContent(
    val text: String,
    val images: List<String> = emptyList()
)

data class PostAuthor(
    val user: User
)

data class PostStats(
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
