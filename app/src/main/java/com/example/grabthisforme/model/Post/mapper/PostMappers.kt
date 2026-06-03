package com.example.grabthisforme.model.post.mapper

import com.example.grabthisforme.model.post.data.dto.PostDto
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.post.data.entity.PostStatsEntity
import com.example.grabthisforme.model.post.data.entity.PostWithAuthorEntity
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.domain.PostAuthor
import com.example.grabthisforme.model.post.domain.PostStats
import com.example.grabthisforme.model.relation.data.entity.UserPostEntity
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity
import org.json.JSONArray

private const val DEFAULT_AUTHOR_NAME = "anonymous"

private fun List<String>.toImagesJson(): String {
    return JSONArray(this).toString()
}

private fun String.toImagesList(): List<String> {
    if (isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.optString(index) }
            .filter { it.isNotBlank() }
    }.getOrDefault(emptyList())
}

fun PostDto.toDomain(): Post {
    return Post(
        postId = postId,
        content = content,
        images = images,
        createTime = createTime,
        author = PostAuthor(
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl
        ),
        likeCount = likeCount,
        commentCount = commentCount
    )
}

fun PostEntity.toDomain(): Post {
    return Post(
        postId = postId,
        content = content,
        images = imagesJson.toImagesList(),
        createTime = createTime,
        author = PostAuthor(
            authorId = 0L,
            authorName = DEFAULT_AUTHOR_NAME,
            authorAvatarUrl = ""
        ),
        likeCount = 0,
        commentCount = 0
    )
}

fun PostWithAuthorEntity.toDomain(stats: PostStats = PostStats()): Post {
    return Post(
        postId = postId,
        content = content,
        images = imagesJson.toImagesList(),
        createTime = createTime,
        author = PostAuthor(
            authorId = authorId,
            authorName = authorName.ifBlank { DEFAULT_AUTHOR_NAME },
            authorAvatarUrl = authorAvatarUrl
        ),
        likeCount = stats.likeCount,
        commentCount = stats.commentCount
    )
}

fun Post.toDto(): PostDto {
    return PostDto(
        postId = postId,
        content = content,
        images = images,
        createTime = createTime,
        authorId = author.authorId,
        authorName = author.authorName,
        authorAvatarUrl = author.authorAvatarUrl,
        likeCount = likeCount,
        commentCount = commentCount
    )
}

fun Post.toEntity(): PostEntity {
    return PostEntity(
        postId = postId,
        content = content,
        imagesJson = images.toImagesJson(),
        createTime = createTime
    )
}

fun Post.toStatsEntity(): PostStatsEntity {
    return PostStatsEntity(
        postId = postId,
        likeCount = likeCount,
        commentCount = commentCount
    )
}

fun PostStatsEntity.toDomain(): PostStats = PostStats(
    likeCount = likeCount,
    commentCount = commentCount
)

fun Post.toUserPostEntity(): UserPostEntity {
    return UserPostEntity(
        userId = author.authorId,
        postId = postId
    )
}

fun Post.toAuthorAccountEntity(): UserAccountEntity {
    val displayName = author.authorName.ifBlank { DEFAULT_AUTHOR_NAME }
    return UserAccountEntity(
        userId = author.authorId,
        accountName = displayName,
        passwordHash = "",
        isCurrent = false,
        isLoginAccount = false,
        createTime = createTime
    )
}

fun Post.toAuthorProfileEntity(): UserProfileEntity {
    val displayName = author.authorName.ifBlank { DEFAULT_AUTHOR_NAME }
    return UserProfileEntity(
        userId = author.authorId,
        displayName = displayName,
        avatarUrl = author.authorAvatarUrl
    )
}
