package com.example.grabthisforme.model.post.data.repository

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.post.data.network.api.PostApi
import com.example.grabthisforme.model.post.data.network.dto.CreateCommentRequest
import com.example.grabthisforme.model.post.data.network.dto.CreatePostRequest
import com.example.grabthisforme.model.post.data.network.dto.CreateReplyRequest
import com.example.grabthisforme.model.post.data.network.dto.SetPostLikedRequest
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.mapper.toDomain
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRemoteRepository @Inject constructor(
    private val postApi: PostApi
) {

    data class CursorPage<T>(
        val items: List<T>,
        val hasMore: Boolean
    )

    suspend fun listPosts(): Result<List<Post>> {
        return runCatching {
            requireSuccessfulData(postApi.listPosts()).map { it.toDomain() }
        }
    }

    suspend fun getPost(postId: String): Result<Post> {
        return runCatching {
            requireSuccessfulData(postApi.getPost(postId)).toDomain()
        }
    }

    suspend fun getComments(postId: String, limit: Int, beforeTime: Long): Result<CursorPage<Comment>> {
        return runCatching {
            val page = requireSuccessfulData(
                postApi.getComments(
                    postId = postId,
                    limit = limit,
                    beforeTime = beforeTime
                )
            )
            CursorPage(
                items = page.items.map { it.toDomain() },
                hasMore = page.hasMore
            )
        }
    }

    suspend fun getReplies(
        postId: String,
        commentId: Long,
        limit: Int,
        beforeTime: Long
    ): Result<CursorPage<Reply>> {
        return runCatching {
            val page = requireSuccessfulData(
                postApi.getReplies(
                    postId = postId,
                    commentId = commentId,
                    limit = limit,
                    beforeTime = beforeTime
                )
            )
            CursorPage(
                items = page.items.map { it.toDomain() },
                hasMore = page.hasMore
            )
        }
    }

    suspend fun createPost(
        content: String,
        images: List<String>,
        categoryKey: String,
        customTags: List<String>
    ): Result<Post> {
        return runCatching {
            requireSuccessfulData(
                postApi.createPost(
                    CreatePostRequest(
                        content = content,
                        images = images,
                        categoryKey = categoryKey,
                        customTags = customTags
                    )
                )
            ).toDomain()
        }
    }

    suspend fun setPostLiked(postId: String, liked: Boolean): Result<Boolean> {
        return runCatching {
            requireSuccessfulData(
                postApi.setPostLiked(
                    postId = postId,
                    request = SetPostLikedRequest(liked = liked)
                )
            )
        }
    }

    suspend fun addComment(postId: String, message: String?, imageUrls: List<String>): Result<Comment> {
        return runCatching {
            requireSuccessfulData(
                postApi.addComment(
                    postId = postId,
                    request = CreateCommentRequest(
                        message = message,
                        imageUrls = imageUrls
                    )
                )
            ).toDomain()
        }
    }

    suspend fun addReply(
        postId: String,
        parentCommentId: Long,
        parentReplyId: Long?,
        message: String?,
        imageUrls: List<String>,
        beCommenterId: Long
    ): Result<Reply> {
        return runCatching {
            requireSuccessfulData(
                postApi.addReply(
                    postId = postId,
                    request = CreateReplyRequest(
                        parentCommentId = parentCommentId,
                        parentReplyId = parentReplyId,
                        message = message,
                        imageUrls = imageUrls,
                        beCommenterId = beCommenterId
                    )
                )
            ).toDomain()
        }
    }

    private fun <T> requireSuccessfulData(response: ApiResponse<T>): T {
        val data = response.data
        if (response.code != 0 || data == null) {
            error(response.message.ifBlank { "Network request failed" })
        }
        return data
    }
}
