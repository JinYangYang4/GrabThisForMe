package com.example.grabthisforme.model.post.mapper

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.CommentDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.dto.ReplyDto
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.mapper.toDomain
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.mapper.toDto
import org.json.JSONArray
import org.json.JSONObject

fun List<Comment>.toCommentsJson(): String {
    val array = JSONArray()
    forEach { comment ->
        array.put(comment.toDto().toJson())
    }
    return array.toString()
}

fun String.toCommentList(): List<Comment> {
    if (isBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(this)
        buildList {
            for (index in 0 until array.length()) {
                val comment = runCatching {
                    array.getJSONObject(index).toCommentDto().toDomain()
                }.getOrNull()
                if (comment != null) {
                    add(comment)
                }
            }
        }
    }.getOrDefault(emptyList())
}

private fun CommentDto.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("time", time)
        putNullableString("message", message)
        put("imageUrls", JSONArray(imageUrls))
        put("commenterId", commenterId)
        put("commenterName", commenterName)
        put("commenterAvatarUrl", commenterAvatarUrl)
        put("replies", JSONArray().apply {
            replies.forEach { put(it.toJson()) }
        })
    }
}

private fun ReplyDto.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("time", time)
        putNullableString("message", message)
        put("commenterId", commenterId)
        put("commenterName", commenterName)
        put("commenterAvatarUrl", commenterAvatarUrl)
        put("beCommenterId", beCommenterId)
        put("beCommenterName", beCommenterName)
        put("beCommenterAvatarUrl", beCommenterAvatarUrl)
        put("imageUrls", JSONArray(imageUrls))
        put("parentCommentId", parentCommentId)
        if (parentReplyId == null) {
            put("parentReplyId", JSONObject.NULL)
        } else {
            put("parentReplyId", parentReplyId)
        }
    }
}

private fun JSONObject.toCommentDto(): CommentDto {
    return CommentDto(
        id = optLong("id"),
        time = optLong("time"),
        message = optNullableString("message"),
        imageUrls = optJsonArray("imageUrls").toStringList(),
        commenterId = optLong("commenterId"),
        commenterName = optString("commenterName"),
        commenterAvatarUrl = optString("commenterAvatarUrl"),
        replies = optJsonArray("replies").toReplyDtoList()
    )
}

private fun JSONObject.toReplyDto(): ReplyDto {
    return ReplyDto(
        id = optLong("id"),
        time = optLong("time"),
        message = optNullableString("message"),
        commenterId = optLong("commenterId"),
        commenterName = optString("commenterName"),
        commenterAvatarUrl = optString("commenterAvatarUrl"),
        beCommenterId = optLong("beCommenterId"),
        beCommenterName = optString("beCommenterName"),
        beCommenterAvatarUrl = optString("beCommenterAvatarUrl"),
        imageUrls = optJsonArray("imageUrls").toStringList(),
        parentCommentId = optLong("parentCommentId"),
        parentReplyId = if (isNull("parentReplyId")) null else optLong("parentReplyId")
    )
}

private fun JSONArray.toReplyDtoList(): List<ReplyDto> {
    return List(length()) { index ->
        getJSONObject(index).toReplyDto()
    }
}

private fun JSONArray.toStringList(): List<String> {
    return List(length()) { index -> optString(index) }
        .filter { it.isNotBlank() }
}

private fun JSONObject.optJsonArray(key: String): JSONArray {
    return optJSONArray(key) ?: JSONArray()
}

private fun JSONObject.optNullableString(key: String): String? {
    if (!has(key) || isNull(key)) return null
    return optString(key)
}

private fun JSONObject.putNullableString(key: String, value: String?) {
    if (value == null) {
        put(key, JSONObject.NULL)
    } else {
        put(key, value)
    }
}
