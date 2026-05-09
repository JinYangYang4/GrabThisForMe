package com.example.grabthisforme.model.post.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_cache",
    indices = [
        Index(value = ["authorId"]),
        Index(value = ["createTime"])
    ]
)
data class PostEntity(
    @PrimaryKey val postId: String,
    val content: String = "",
    val imagesJson: String = "[]",
    val createTime: Long = 0L,
    val authorId: Long = 0L,
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
