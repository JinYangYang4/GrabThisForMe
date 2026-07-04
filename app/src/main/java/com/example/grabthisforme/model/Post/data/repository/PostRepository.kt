package com.example.grabthisforme.model.post.data.repository
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.domain.Post
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@Singleton
class PostRepository @Inject constructor(
    private val localRepository: PostLocalRepository,
    private val remoteRepository: PostRemoteRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _allPostList = MutableStateFlow<List<Post>>(emptyList())

    val allPostList = _allPostList.asStateFlow()
    val myPostList = localRepository.myPostList
    val likedPostList = localRepository.likedPostList

    suspend fun refreshPosts(): List<Post> {
        val result = remoteRepository.listPosts(
            limit = DEFAULT_POST_PAGE_SIZE,
            beforeTime = System.currentTimeMillis() + 1L,
            categoryKey = null
        )
        result.onSuccess { page ->
            localRepository.savePosts(page.items)
            _allPostList.value = page.items.sortedByDescending { it.createTime }
        }
        return result.getOrElse {
            val localPosts = localRepository.allPostList.value
            _allPostList.value = localPosts
            PostRemoteRepository.CursorPage(
                items = localPosts,
                hasMore = false
            )
        }.items
    }

    suspend fun getPostPage(
        limit: Int = DEFAULT_POST_PAGE_SIZE,
        beforeTime: Long,
        categoryKey: String? = null
    ): PostRemoteRepository.CursorPage<Post> {
        return remoteRepository.listPosts(
            limit = limit,
            beforeTime = beforeTime,
            categoryKey = categoryKey?.takeIf { it.isNotBlank() }
        ).onSuccess { page ->
            page.items.forEach { localRepository.savePost(it) }
        }.getOrElse {
            buildLocalPostPage(
                limit = limit,
                beforeTime = beforeTime,
                categoryKey = categoryKey
            )
        }
    }

    fun getPost(postId: String): Flow<Post?> = flow {
        remoteRepository.getPost(postId)
            .onSuccess { remotePost ->
                localRepository.savePost(remotePost)
            }
            .onFailure {
            }
        emitAll(localRepository.getPost(postId))
    }

    fun getCommentList(postId: String): Flow<List<Comment>> = localRepository.getCommentList(postId)

    suspend fun getCommentPage(
        postId: String,
        limit: Int = 50,
        beforeTime: Long
    ): PostRemoteRepository.CursorPage<Comment> {
        return remoteRepository.getComments(postId, limit, beforeTime)
            .onSuccess { page ->
                localRepository.cacheComments(postId, page.items)
            }
            .onFailure {
            }
            .getOrElse {
                PostRemoteRepository.CursorPage(
                    items = localRepository.getCommentPage(postId, limit, beforeTime),
                    hasMore = false
                )
            }
    }

    suspend fun getReplyPage(
        postId: String,
        commentId: Long,
        limit: Int = 50,
        beforeTime: Long
    ): PostRemoteRepository.CursorPage<Reply> {
        return remoteRepository.getReplies(postId, commentId, limit, beforeTime)
            .onSuccess { page ->
                localRepository.cacheReplies(postId, commentId, page.items)
            }
            .getOrElse {
                PostRemoteRepository.CursorPage(
                    items = localRepository.getReplyPage(commentId, limit),
                    hasMore = false
                )
            }
    }

    fun isPostLiked(postId: String): Flow<Boolean> = localRepository.isPostLiked(postId)

    suspend fun savePost(post: Post) {
        localRepository.savePost(post)
        upsertAllPostList(post)
    }

    suspend fun savePosts(posts: List<Post>) {
        localRepository.savePosts(posts)
        _allPostList.value = posts.sortedByDescending { it.createTime }
    }

    suspend fun deletePost(postId: String) {
        localRepository.deletePost(postId)
        _allPostList.value = _allPostList.value.filterNot { it.postId == postId }
    }

    suspend fun addComment(postId: String, comment: Comment): Result<Comment> {
        return remoteRepository.addComment(
            postId = postId,
            message = comment.message,
            imageUrls = comment.imageUrls,
            commenterProvince = comment.commenterProvince
        ).onSuccess { remoteComment ->
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
                updateAllPostListLikeState(postId, liked)
            }
            .getOrElse {
                localRepository.setPostLiked(postId, liked).also {
                    updateAllPostListLikeState(postId, liked)
                }
            }
    }

    suspend fun publishPost(
        content: String,
        images: List<String> = emptyList(),
        categoryKey: String = "",
        customTags: List<String> = emptyList(),
        latitude: Double? = null,
        longitude: Double? = null,
        country: String = "",
        province: String = "",
        city: String = "",
        district: String = "",
        locationLabel: String = ""
    ): Post {
        return remoteRepository.createPost(
            content = content,
            images = images,
            categoryKey = categoryKey,
            customTags = customTags,
            latitude = latitude,
            longitude = longitude,
            country = country,
            province = province,
            city = city,
            district = district,
            locationLabel = locationLabel
        ).onSuccess { remotePost ->
            localRepository.savePost(remotePost)
            upsertAllPostList(remotePost)
        }.getOrElse {
            localRepository.publishPost(
                content = content,
                images = images,
                categoryKey = categoryKey,
                customTags = customTags,
                latitude = latitude,
                longitude = longitude,
                country = country,
                province = province,
                city = city,
                district = district,
                locationLabel = locationLabel
            ).also { localPost ->
                upsertAllPostList(localPost)
            }
        }
    }

    private fun upsertAllPostList(post: Post) {
        _allPostList.value = (_allPostList.value + post)
            .distinctBy { it.postId }
            .sortedByDescending { it.createTime }
    }

    private fun updateAllPostListLikeState(postId: String, liked: Boolean) {
        _allPostList.value = _allPostList.value.map { post ->
            if (post.postId != postId) {
                post
            } else {
                val updatedLikeCount = if (liked) {
                    post.likeCount + 1
                } else {
                    (post.likeCount - 1).coerceAtLeast(0)
                }
                Post(
                    identity = post.identity,
                    contentInfo = post.contentInfo,
                    authorInfo = post.authorInfo,
                    statsInfo = post.statsInfo.copy(likeCount = updatedLikeCount)
                )
            }
        }
    }

    private fun buildLocalPostPage(
        limit: Int,
        beforeTime: Long,
        categoryKey: String?
    ): PostRemoteRepository.CursorPage<Post> {
        val normalizedLimit = limit.coerceAtLeast(1)
        val safeBeforeTime = if (beforeTime <= 0L) {
            System.currentTimeMillis() + 1L
        } else {
            beforeTime
        }
        val filtered = localRepository.allPostList.value
            .asSequence()
            .filter { it.createTime < safeBeforeTime }
            .filter { categoryKey.isNullOrBlank() || it.categoryKey == categoryKey }
            .sortedByDescending { it.createTime }
            .toList()
        val items = filtered.take(normalizedLimit)
        return PostRemoteRepository.CursorPage(
            items = items,
            hasMore = filtered.size > normalizedLimit
        )
    }

    companion object {
        private const val DEFAULT_POST_PAGE_SIZE = 20
    }
}

