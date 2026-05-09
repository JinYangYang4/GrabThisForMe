package com.example.grabthisforme.model.post.mapper

import org.json.JSONArray
import com.example.grabthisforme.model.post.data.dto.PostDto
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.domain.PostAuthor

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
            authorId = authorId,
            authorName = authorName.ifBlank { "Anonymous" },
            authorAvatarUrl = authorAvatarUrl
        ),
        likeCount = likeCount,
        commentCount = commentCount
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
        createTime = createTime,
        authorId = author.authorId,
        authorName = author.authorName,
        authorAvatarUrl = author.authorAvatarUrl,
        likeCount = likeCount,
        commentCount = commentCount
    )
}
