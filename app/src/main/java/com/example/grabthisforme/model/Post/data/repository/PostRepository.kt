package com.example.grabthisforme.model.post.data.repository

import android.util.Log
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.domain.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val localRepository: PostLocalRepository,
    private val remoteRepository: PostRemoteRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val allPostList = localRepository.allPostList
    val myPostList = localRepository.myPostList
    val likedPostList = localRepository.likedPostList

    init {
        repositoryScope.launch {
            refreshPosts()
        }
    }

    suspend fun refreshPosts(): List<Post> {
        return remoteRepository.listPosts()
            .onSuccess { remotePosts ->
                localRepository.savePosts(remotePosts)
            }
            .getOrElse {
                localRepository.allPostList.value
            }
    }

    fun getPost(postId: String): Flow<Post?> = flow {
        remoteRepository.getPost(postId)
            .onSuccess { remotePost ->
                localRepository.savePost(remotePost)
            }
        emitAll(localRepository.getPost(postId))
    }

    fun getCommentList(postId: String): Flow<List<Comment>> = localRepository.getCommentList(postId)

    suspend fun getCommentListOnce(postId: String): List<Comment> {
        return remoteRepository.getComments(postId, limit = 20, offset = 0)
            .onSuccess { comments ->
                comments.forEach { comment ->
                    localRepository.addComment(postId, comment)
                }
            }
            .getOrElse {
                localRepository.getCommentListOnce(postId)
            }
    }

    suspend fun getCommentPage(postId: String, limit: Int = 50, offset: Int = 0): List<Comment> {
        return remoteRepository.getComments(postId, limit, offset)
            .getOrElse {
                localRepository.getCommentPage(postId, limit, offset)
            }
    }

    suspend fun getReplyPage(
        postId: String,
        commentId: Long,
        limit: Int = 50,
        offset: Int = 0
    ): List<Reply> {
        return remoteRepository.getReplies(postId, commentId, limit, offset)
            .getOrElse { emptyList() }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = localRepository.isPostLiked(postId)

    suspend fun savePost(post: Post) = localRepository.savePost(post)
    suspend fun savePosts(posts: List<Post>) = localRepository.savePosts(posts)
    suspend fun deletePost(postId: String) = localRepository.deletePost(postId)

    suspend fun addComment(postId: String, comment: Comment): List<Comment> {
        remoteRepository.addComment(postId, comment.message, comment.imageUrls)
            .onSuccess { remoteComment ->
                localRepository.addComment(postId, remoteComment)
            }
        return localRepository.getCommentListOnce(postId)
    }

    suspend fun addReply(
        postId: String,
        parentCommentId: Long,
        reply: Reply,
        beCommenterId: Long
    ): List<Comment> {
        remoteRepository.addReply(
            postId = postId,
            parentCommentId = parentCommentId,
            parentReplyId = reply.parentReplyId,
            message = reply.message,
            imageUrls = reply.imageUrls,
            beCommenterId = beCommenterId
        ).onSuccess { remoteReply ->
            val paramsLog = buildString {
                appendLine("=====addReply请求参数=====")
                appendLine("postId: $postId")
                appendLine("parentCommentId: $parentCommentId")
                appendLine("parentReplyId: ${reply.parentReplyId}")
                appendLine("message: ${reply.message}")
                appendLine("imageUrls: ${reply.imageUrls}")
                appendLine("beCommenterId: $beCommenterId")
                appendLine("=========================")
            }
            Log.e("test11", paramsLog)
            localRepository.addReply(postId, parentCommentId, remoteReply)
        }.onFailure { err ->
            // 打印请求入参所有字段
            val paramsLog = buildString {
                appendLine("=====addReply请求参数=====")
                appendLine("postId: $postId")
                appendLine("parentCommentId: $parentCommentId")
                appendLine("parentReplyId: ${reply.parentReplyId}")
                appendLine("message: ${reply.message}")
                appendLine("imageUrls: ${reply.imageUrls}")
                appendLine("beCommenterId: $beCommenterId")
                appendLine("=========================")
            }
            Log.e("test11", paramsLog, err)
        }
            return localRepository.getCommentListOnce(postId)
    }

    suspend fun setPostLiked(postId: String, liked: Boolean): Boolean {
        return remoteRepository.setPostLiked(postId, liked)
            .onSuccess {
                localRepository.setPostLiked(postId, liked)
            }
            .getOrElse {
                localRepository.setPostLiked(postId, liked)
            }
    }

    suspend fun publishPost(content: String, images: List<String> = emptyList()): Post {
        return remoteRepository.createPost(content, images)
            .onSuccess { remotePost ->
                localRepository.savePost(remotePost)
            }
            .getOrElse {
                localRepository.publishPost(content, images)
            }
    }
}
