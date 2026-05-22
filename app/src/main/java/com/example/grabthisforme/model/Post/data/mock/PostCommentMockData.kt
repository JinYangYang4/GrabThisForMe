package com.example.grabthisforme.model.post.data.mock

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.data.mock.PostDetailMockData
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment

object PostCommentMockData {

    fun buildCommentList(targetCount: Int): List<Comment> {
        if (targetCount <= 0) return emptyList()
        val templateComments = PostDetailMockData.getMockCommentList()
        if (templateComments.isEmpty()) return emptyList()

        return List(targetCount) { index ->
            val template = templateComments[index % templateComments.size]
            val commentOffset = (index + 1) * 10_000L
            val newCommentId = template.id + commentOffset
            template.copy(
                id = newCommentId,
                time = template.time - (index * 60_000L),
                replies = template.replies.map { reply ->
                    reply.copy(
                        id = reply.id + commentOffset,
                        time = reply.time - (index * 30_000L),
                        parentCommentId = newCommentId,
                        parentReplyId = reply.parentReplyId?.plus(commentOffset)
                    )
                }
            )
        }
    }
}
