package com.example.grabthisforme.model.post.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_cache",
    indices = [
        Index(value = ["createTime"])
    ]
)
data class PostEntity(
    @PrimaryKey val postId: String,
    val content: String = "",
    val imagesJson: String = "[]",
    val createTime: Long = 0L
)

@Entity(
    tableName = "post_stats",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["postId"])
    ]
)
data class PostStatsEntity(
    @PrimaryKey val postId: String,
    val likeCount: Int = 0,
    val commentCount: Int = 0
)

data class PostWithAuthorEntity(
    val postId: String,
    val content: String = "",
    val imagesJson: String = "[]",
    val createTime: Long = 0L,
    val authorId: Long = 0L,
    val authorName: String = "",
    val authorAvatarUrl: String = ""
)
