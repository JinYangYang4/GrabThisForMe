package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui_model

import com.example.grabthisforme.model.post.domain.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PostDetailHeaderUiModel(
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val timeText: String = "",
    val locationText: String = "",
    val contentText: String = "",
    val imageUrls: List<String> = emptyList()
)

data class PostDetailStatsUiModel(
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likeText: String = "点赞",
    val commentText: String = "评论"
)

fun Post.toPostDetailHeaderUiModel(): PostDetailHeaderUiModel {
    return PostDetailHeaderUiModel(
        authorName = authorName.ifBlank { "匿名" },
        authorAvatarUrl = authorAvatarUrl,
        timeText = formatPostDetailTime(createTime),
        locationText = buildLocationText(),
        contentText = content,
        imageUrls = images.map { it.trim() }.filter { it.isNotEmpty() }
    )
}

fun Post.toPostDetailStatsUiModel(): PostDetailStatsUiModel {
    return PostDetailStatsUiModel(
        likeCount = likeCount,
        commentCount = commentCount,
        likeText = buildPostDetailCountText("点赞", likeCount),
        commentText = buildPostDetailCountText("评论", commentCount)
    )
}

fun buildPostDetailStatsUiModel(
    likeCount: Int,
    commentCount: Int
): PostDetailStatsUiModel {
    return PostDetailStatsUiModel(
        likeCount = likeCount,
        commentCount = commentCount,
        likeText = buildPostDetailCountText("点赞", likeCount),
        commentText = buildPostDetailCountText("评论", commentCount)
    )
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

private fun formatPostDetailTime(createTime: Long): String {
    if (createTime <= 0L) return ""
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return "发布于 ${formatter.format(Date(createTime))}"
}

private fun buildPostDetailCountText(prefix: String, count: Int): String {
    return if (count > 0) "$prefix $count" else prefix
}
