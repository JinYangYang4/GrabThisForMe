package com.example.grabthisforme.model.post.data.dto

data class PostDto(
    val postId: String,
    val content: String,
    val images: List<String> = emptyList(),
    val createTime: Long,
    val authorId: Long = 0L,
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
