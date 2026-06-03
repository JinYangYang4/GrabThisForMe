package com.example.grabthisforme.activity.communityFragment.ui_model

import com.example.grabthisforme.model.post.domain.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PostCardUiModel(
    val postId: String,
    val authorName: String,
    val authorAvatarUrl: String,
    val timeText: String,
    val tagText: String,
    val contentText: String,
    val imageUrls: List<String>
)

fun Post.toPostCardUiModel(
    tagText: String = "乐于助人"
): PostCardUiModel {
    val cleanImages = images
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return PostCardUiModel(
        postId = postId,
        authorName = authorName.ifBlank { "匿名用户" },
        authorAvatarUrl = authorAvatarUrl,
        timeText = formatPostCardTime(createTime),
        tagText = tagText,
        contentText = content,
        imageUrls = cleanImages
    )
}

private fun formatPostCardTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val timeInMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}
