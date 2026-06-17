package com.example.grabthisforme.model.post.data.network.dto

import com.example.grabthisforme.model.user.data.network.dto.UserDto

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

data class PostDetailDto(
    val postId: String,
    val content: String,
    val images: List<String> = emptyList(),
    val createTime: Long,
    val author: UserDto,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedByCurrentUser: Boolean = false
)

data class PostCommentDto(
    val commentId: Long,
    val time: Long,
    val message: String? = null,
    val imageUrls: List<String> = emptyList(),
    val commenter: UserDto,
    val replyCount: Int = 0
)

data class PostReplyDto(
    val replyId: Long,
    val parentCommentId: Long,
    val parentReplyId: Long? = null,
    val time: Long,
    val message: String? = null,
    val imageUrls: List<String> = emptyList(),
    val commenter: UserDto,
    val beCommenter: UserDto
)

data class CreatePostRequest(
    val content: String,
    val images: List<String> = emptyList()
)

data class SetPostLikedRequest(
    val liked: Boolean
)

data class CreateCommentRequest(
    val message: String? = null,
    val imageUrls: List<String> = emptyList()
)

data class CreateReplyRequest(
    val parentCommentId: Long,
    val parentReplyId: Long? = null,
    val message: String? = null,
    val imageUrls: List<String> = emptyList(),
    val beCommenterId: Long
)
