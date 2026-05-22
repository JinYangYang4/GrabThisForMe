package com.example.grabthisforme.model.post.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "post_comment_cache",
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["postId"], unique = true)]
)
data class PostCommentEntity(
    @PrimaryKey val postId: String,
    val commentsJson: String = "[]"
)
