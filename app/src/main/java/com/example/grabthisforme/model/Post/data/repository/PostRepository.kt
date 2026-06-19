package com.example.grabthisforme.model.post.data.repository

import android.util.Log
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.domain.Post
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

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
            .onFailure {
                Log.e("com.example.grabthisforme.model.post.data.repository.getPost", "e:${it.message}")
            }
        emitAll(localRepository.getPost(postId))
    }

    fun getCommentList(postId: String): Flow<List<Comment>> = localRepository.getCommentList(postId)

    suspend fun getCommentListOnce(postId: String): List<Comment> {
        return remoteRepository.getComments(postId, limit = 20, offset = 0)
            .onSuccess { comments ->
                localRepository.cacheComments(postId, comments)
            }
            .onFailure {
                Log.e("com.example.grabthisforme.model.post.data.repository.getCommentListOnce", "e:${it.message}")
            }
            .getOrElse {
                localRepository.getCommentListOnce(postId)
            }
    }

    suspend fun getCommentPage(postId: String, limit: Int = 50, offset: Int = 0): List<Comment> {
        return remoteRepository.getComments(postId, limit, offset)
            .onSuccess { comments ->
                if (offset == 0) {
                    localRepository.cacheComments(postId, comments)
                }
            }
            .getOrElse {
                localRepository.getCommentPage(postId, limit, offset)
            }
    }

    suspend fun getReplyPage(
        postId: String,
        commentId: Long,
        limit: Int = 50,
        beforeTime: Long
    ): List<Reply> {
        return remoteRepository.getReplies(postId, commentId, limit, beforeTime)
            .onSuccess { replies ->
                localRepository.cacheReplies(postId, commentId, replies)
                Log.d("test11", "getReplyPage: ${replies.size}")
            }
            .getOrElse {
                Log.d("test11", "getReplyPage: ${it.message}")
                localRepository.getReplyPage(commentId, limit)
            }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = localRepository.isPostLiked(postId)

    suspend fun savePost(post: Post) = localRepository.savePost(post)
    suspend fun savePosts(posts: List<Post>) = localRepository.savePosts(posts)
    suspend fun deletePost(postId: String) = localRepository.deletePost(postId)

    suspend fun addComment(postId: String, comment: Comment): Result<Comment> {
        return remoteRepository.addComment(postId, comment.message, comment.imageUrls)
            .onSuccess { remoteComment ->
                localRepository.addComment(postId, remoteComment)
            }
    }

    suspend fun addReply(
        postId: String,
        parentCommentId: Long,
        reply: Reply,
        beCommenterId: Long
    ): Result<Reply> {
        return remoteRepository.addReply(
            postId = postId,
            parentCommentId = parentCommentId,
            parentReplyId = reply.parentReplyId,
            message = reply.message,
            imageUrls = reply.imageUrls,
            beCommenterId = beCommenterId
        ).onSuccess { remoteReply ->
            localRepository.addReply(postId, parentCommentId, remoteReply)
        }
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
