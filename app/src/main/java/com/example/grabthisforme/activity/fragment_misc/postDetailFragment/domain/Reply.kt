package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain

import com.example.grabthisforme.model.user.domain.User

data class Reply(
    val id: Long,
    val time: Long,
    val message: String? = null,
    val commenter: User? = null,
    val beCommenter: User? = null,
    val imageUrls: List<String> = emptyList(),
    val parentCommentId: Long = 0,
    val parentReplyId: Long? = null,
    val sendStatus: LocalSendStatus = LocalSendStatus.NONE
)
