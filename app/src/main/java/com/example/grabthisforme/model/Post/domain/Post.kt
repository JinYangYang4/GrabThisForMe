package com.example.grabthisforme.model.post.domain

import com.example.grabthisforme.model.user.domain.User

data class Post(
    val identity: PostIdentity,
    val contentInfo: PostContent,
    val authorInfo: PostAuthor,
    val statsInfo: PostStats
) {
    val postId: String get() = identity.postId
    val content: String get() = contentInfo.text
    val images: List<String> get() = contentInfo.images
    val createTime: Long get() = identity.createTime
    val author: User get() = authorInfo.user
    val likeCount: Int get() = statsInfo.likeCount
    val commentCount: Int get() = statsInfo.commentCount

    constructor(
        postId: String,
        content: String,
        images: List<String> = emptyList(),
        createTime: Long,
        author: User,
        likeCount: Int,
        commentCount: Int
    ) : this(
        identity = PostIdentity(
            postId = postId,
            createTime = createTime
        ),
        contentInfo = PostContent(
            text = content,
            images = images
        ),
        authorInfo = PostAuthor(
            user = author
        ),
        statsInfo = PostStats(
            likeCount = likeCount,
            commentCount = commentCount
        )
    )

    fun hasImages(): Boolean = images.isNotEmpty()
}
