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
    val imageUrls: List<String>,
    val locationText: String
)

fun Post.toPostCardUiModel(): PostCardUiModel {
    val cleanImages = images
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return PostCardUiModel(
        postId = postId,
        authorName = authorName.ifBlank { "匿名用户" },
        authorAvatarUrl = authorAvatarUrl,
        timeText = formatPostCardTime(createTime),
        tagText = buildTagText(),
        contentText = content,
        imageUrls = cleanImages,
        locationText = buildLocationText()
    )
}

private fun Post.buildTagText(): String {
    val categoryLabel = categoryKey.toCategoryLabel()
    val tags = customTags.filter { it.isNotBlank() }
    return buildList {
        if (categoryLabel.isNotBlank()) add(categoryLabel)
        addAll(tags.take(2))
    }.joinToString(" · ").ifBlank { "校园话题" }
}

private fun Post.buildLocationText(): String {
    return locationLabel.ifBlank {
        if (country.isNotBlank() && !country.contains("中国") && !country.equals("China", ignoreCase = true)) {
            country
        } else {
            listOf(province, city, district)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" ")
        }
    }
}

private fun String.toCategoryLabel(): String {
    return when (this) {
        "FUNNY" -> "搞笑"
        "GOSSIP" -> "吐槽"
        "SHARE" -> "分享"
        "FRESH" -> "新鲜"
        "SECOND_HAND" -> "二手"
        "MAKE_FRIENDS" -> "交友"
        "GAME" -> "游戏"
        "LOST_FOUND" -> "失物"
        "CLUB" -> "社团"
        "FOOD" -> "美食"
        "WARNING" -> "避雷"
        "QUESTION" -> "疑问"
        else -> ""
    }
}

private fun formatPostCardTime(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val timeInMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}
