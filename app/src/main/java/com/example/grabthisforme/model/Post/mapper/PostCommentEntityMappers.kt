package com.example.grabthisforme.model.post.mapper

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.data.local.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.local.entity.PostReplyEntity
import com.example.grabthisforme.model.user.domain.User
import org.json.JSONArray

fun Comment.toEntity(postId: String): PostCommentEntity {
    return PostCommentEntity(
        commentId = id,
        postId = postId,
        time = time,
        message = message,
        imageUrlsJson = imageUrls.toJsonArrayString(),
        commenterId = commenter?.id ?: 0L,
        commenterName = commenter?.name.orEmpty(),
        commenterAvatarUrl = commenter?.headPic.orEmpty(),
        commenterProvince = commenterProvince
    )
}

fun Reply.toEntity(postId: String): PostReplyEntity {
    return PostReplyEntity(
        replyId = id,
        postId = postId,
        parentCommentId = parentCommentId,
        parentReplyId = parentReplyId,
        time = time,
        message = message,
        imageUrlsJson = imageUrls.toJsonArrayString(),
        commenterId = commenter?.id ?: 0L,
        commenterName = commenter?.name.orEmpty(),
        commenterAvatarUrl = commenter?.headPic.orEmpty(),
        beCommenterId = beCommenter?.id ?: 0L,
        beCommenterName = beCommenter?.name.orEmpty(),
        beCommenterAvatarUrl = beCommenter?.headPic.orEmpty()
    )
}

fun PostCommentEntity.toDomain(replies: List<Reply> = emptyList()): Comment {
    return Comment(
        id = commentId,
        time = time,
        message = message,
        imageUrls = imageUrlsJson.toStringList(),
        commenter = buildUser(commenterId, commenterName, commenterAvatarUrl),
        replies = replies,
        replyCount = replies.size,
        commenterProvince = commenterProvince
    )
}

fun PostReplyEntity.toDomain(): Reply {
    return Reply(
        id = replyId,
        time = time,
        message = message,
        commenter = buildUser(commenterId, commenterName, commenterAvatarUrl),
        beCommenter = buildUser(beCommenterId, beCommenterName, beCommenterAvatarUrl),
        imageUrls = imageUrlsJson.toStringList(),
        parentCommentId = parentCommentId,
        parentReplyId = parentReplyId
    )
}

private fun buildUser(userId: Long, name: String, avatarUrl: String): User? {
    if (userId <= 0L && name.isBlank() && avatarUrl.isBlank()) return null
    return User(
        id = userId,
        name = name,
        headPic = avatarUrl
    )
}

private fun List<String>.toJsonArrayString(): String {
    return JSONArray(filter { it.isNotBlank() }).toString()
}

private fun String.toStringList(): List<String> {
    if (isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(this)
        List(array.length()) { index -> array.optString(index) }
            .filter { it.isNotBlank() }
    }.getOrDefault(emptyList())
}
