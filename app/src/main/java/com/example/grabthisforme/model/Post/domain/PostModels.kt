package com.example.grabthisforme.model.post.domain

data class PostIdentity(
    val postId: String,
    val createTime: Long
)

data class PostContent(
    val text: String,
    val images: List<String> = emptyList(),
    val categoryKey: String = "",
    val customTags: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val country: String = "",
    val province: String = "",
    val city: String = "",
    val district: String = "",
    val locationLabel: String = ""
)

data class PostAuthor(
    val authorId: Long,
    val authorName: String = "",
    val authorAvatarUrl: String = ""
)

data class PostStats(
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
