package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain

import com.example.grabthisforme.model.user.domain.User

data class Comment(
    val id: Long,
    val time: Long,
    val message: String? = null,
    val imageUrls: List<String> = emptyList(),
    val commenter: User? = null,
    val replies: List<Reply> = emptyList(),
    val replyCount: Int = replies.size,
    val commenterProvince: String = ""
) {
    val hasReplies: Boolean get() = replyCount > 0 || replies.isNotEmpty()
}
