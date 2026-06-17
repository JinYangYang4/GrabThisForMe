package com.example.grabthisforme.model.post.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "post_comment",
    primaryKeys = ["commentId"],
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["postId", "time"]),
        Index(value = ["commenterId"])
    ]
)
data class PostCommentEntity(
    val commentId: Long,
    val postId: String,
    val time: Long,
    val message: String? = null,
    val imageUrlsJson: String = "[]",
    val commenterId: Long = 0L,
    val commenterName: String = "",
    val commenterAvatarUrl: String = ""
)

@Entity(
    tableName = "post_reply",
    primaryKeys = ["replyId"],
    foreignKeys = [
        ForeignKey(
            entity = PostEntity::class,
            parentColumns = ["postId"],
            childColumns = ["postId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PostCommentEntity::class,
            parentColumns = ["commentId"],
            childColumns = ["parentCommentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["postId"]),
        Index(value = ["parentCommentId", "time"]),
        Index(value = ["commenterId"]),
        Index(value = ["beCommenterId"])
    ]
)
data class PostReplyEntity(
    val replyId: Long,
    val postId: String,
    val parentCommentId: Long,
    val parentReplyId: Long? = null,
    val time: Long,
    val message: String? = null,
    val imageUrlsJson: String = "[]",
    val commenterId: Long = 0L,
    val commenterName: String = "",
    val commenterAvatarUrl: String = "",
    val beCommenterId: Long = 0L,
    val beCommenterName: String = "",
    val beCommenterAvatarUrl: String = ""
)
