package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto

data class ReplyDto(
    val id: Long,
    val time: Long,
    val message: String? = null,
    val commenterId: Long = 0L,
    val commenterName: String = "",
    val commenterAvatarUrl: String = "",
    val beCommenterId: Long = 0L,
    val beCommenterName: String = "",
    val beCommenterAvatarUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val parentCommentId: Long = 0,
    val parentReplyId: Long? = null
)
