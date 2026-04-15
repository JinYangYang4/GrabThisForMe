package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model

import com.example.grabthisforme.model.user.User

data class Reply(
    val id: Long,
    val time: Long,
    val message: String? = null,
    val commenter: User? = null,
    val beCommenter: User? = null,
    val imageUrls: MutableList<String>? = null,
    val parentCommentId: Long = 0,
    val parentReplyId: Long? = null
)