package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.mapper

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.CommentDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.ReplyDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.user.domain.User

private fun buildUser(
    userId: Long,
    name: String,
    avatarUrl: String
): User {
    return User(
        id = userId,
        name = name,
        headPic = avatarUrl
    )
}

fun ReplyDto.toDomain(): Reply {
    return Reply(
        id = id,
        time = time,
        message = message,
        commenter = buildUser(
            userId = commenterId,
            name = commenterName,
            avatarUrl = commenterAvatarUrl
        ),
        beCommenter = buildUser(
            userId = beCommenterId,
            name = beCommenterName,
            avatarUrl = beCommenterAvatarUrl
        ),
        imageUrls = imageUrls,
        parentCommentId = parentCommentId,
        parentReplyId = parentReplyId
    )
}

fun Reply.toDto(): ReplyDto {
    return ReplyDto(
        id = id,
        time = time,
        message = message,
        commenterId = commenter?.id ?: 0L,
        commenterName = commenter?.name.orEmpty(),
        commenterAvatarUrl = commenter?.headPic.orEmpty(),
        beCommenterId = beCommenter?.id ?: 0L,
        beCommenterName = beCommenter?.name.orEmpty(),
        beCommenterAvatarUrl = beCommenter?.headPic.orEmpty(),
        imageUrls = imageUrls,
        parentCommentId = parentCommentId,
        parentReplyId = parentReplyId
    )
}

fun CommentDto.toDomain(): Comment {
    return Comment(
        id = id,
        time = time,
        message = message,
        imageUrls = imageUrls,
        commenter = buildUser(
            userId = commenterId,
            name = commenterName,
            avatarUrl = commenterAvatarUrl
        ),
        replies = replies.map { it.toDomain() }
    )
}

fun Comment.toDto(): CommentDto {
    return CommentDto(
        id = id,
        time = time,
        message = message,
        imageUrls = imageUrls,
        commenterId = commenter?.id ?: 0L,
        commenterName = commenter?.name.orEmpty(),
        commenterAvatarUrl = commenter?.headPic.orEmpty(),
        replies = replies.map { it.toDto() }
    )
}
