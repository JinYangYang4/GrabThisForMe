package com.example.grabthisforme.model.post.mapper

import com.example.grabthisforme.model.post.data.dto.PostDto
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.user.domain.User

fun PostDto.toDomain(): Post {
    return Post(
        postId = postId,
        content = content,
        images = images,
        createTime = createTime,
        author = User(
            id = authorId,
            name = authorName,
            headPic = authorAvatarUrl
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
        authorId = author.id,
        authorName = author.name,
        authorAvatarUrl = author.headPic,
        likeCount = likeCount,
        commentCount = commentCount
    )
}
