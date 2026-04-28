package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto

data class CommentDto(
    val id: Long,
    val time: Long,
    val message: String? = null,
    val imageUrls: List<String> = emptyList(),
    val commenterId: Long = 0L,
    val commenterName: String = "",
    val commenterAvatarUrl: String = "",
    val replies: List<ReplyDto> = emptyList()
)
