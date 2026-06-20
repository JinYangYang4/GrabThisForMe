package com.example.grabthisforme.model.post.data.repository

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.data.local.dao.PostDao
import com.example.grabthisforme.model.post.data.local.dao.PostStatsDao
import com.example.grabthisforme.model.post.data.local.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.local.entity.PostStatsEntity
import com.example.grabthisforme.model.post.data.local.entity.PostWithAuthorEntity
import com.example.grabthisforme.model.post.data.mock.PostCommentMockData
import com.example.grabthisforme.model.post.data.mock.PostMockData
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.domain.PostAuthor
import com.example.grabthisforme.model.post.domain.PostStats
import com.example.grabthisforme.model.post.mapper.toAuthorAccountEntity
import com.example.grabthisforme.model.post.mapper.toAuthorProfileEntity
import com.example.grabthisforme.model.post.mapper.toDomain
import com.example.grabthisforme.model.post.mapper.toEntity
import com.example.grabthisforme.model.post.mapper.toStatsEntity
import com.example.grabthisforme.model.post.mapper.toUserPostEntity
import com.example.grabthisforme.model.relation.data.dao.UserRelationDao
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.user.data.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class PostLocalRepository @Inject constructor(
    private val postDao: PostDao,
    private val postStatsDao: PostStatsDao,
    private val userRelationDao: UserRelationDao,
    private val userRepository: UserRepository
) {
    companion object {
        private const val MAX_CACHED_POSTS = 10
        private const val MAX_CACHED_COMMENTS_PER_POST = 30
        private const val MAX_CACHED_REPLIES_PER_COMMENT = 5
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val commentCacheMutex = Mutex()
    private val replyCacheMutex = Mutex()

    private val sourcePosts: StateFlow<List<Post>> = observePostList(postDao.getAllPostWithAuthorsFlow())
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allPostList: StateFlow<List<Post>> = sourcePosts

    val myPostList: StateFlow<List<Post>> = userRepository.currentUserId.flatMapLatest { currentUserId ->
        if (currentUserId == null) {
            flowOf(emptyList())
        } else {
            observePostList(postDao.getPostsByUserId(currentUserId))
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val likedPostList: StateFlow<List<Post>> = userRepository.currentUserId.flatMapLatest { currentUserId ->
        if (currentUserId == null) {
            flowOf(emptyList())
        } else {
            observePostList(postDao.getLikedPostsByUserId(currentUserId))
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        repositoryScope.launch {
            val cachedPosts = postDao.observeAllPostEntities().first()
            if (cachedPosts.isEmpty()) {
                val mockPosts = PostMockData.getPostList().take(MAX_CACHED_POSTS)
                savePosts(mockPosts)
                mockPosts.forEach { post ->
                    val mockComments = PostCommentMockData.buildCommentList(post.commentCount)
                        .take(MAX_CACHED_COMMENTS_PER_POST)
                    cacheComments(post.postId, mockComments)
                }
            }
        }
    }

    suspend fun savePost(post: Post) {
        postDao.insertAuthorAccountIfAbsent(post.toAuthorAccountEntity())
        postDao.insertAuthorProfileIfAbsent(post.toAuthorProfileEntity())
        postDao.upsert(post.toEntity())
        postStatsDao.upsert(post.toStatsEntity())
        userRelationDao.upsertUserPost(post.toUserPostEntity())
        postDao.trimPosts(MAX_CACHED_POSTS)
    }

    suspend fun savePosts(posts: List<Post>) {
        val limitedPosts = posts.sortedByDescending { it.createTime }.take(MAX_CACHED_POSTS)
        postDao.insertAuthorAccountsIfAbsent(limitedPosts.map { it.toAuthorAccountEntity() })
        postDao.insertAuthorProfilesIfAbsent(limitedPosts.map { it.toAuthorProfileEntity() })
        postDao.upsertAll(limitedPosts.map { it.toEntity() })
        postStatsDao.upsertAll(limitedPosts.map { it.toStatsEntity() })
        userRelationDao.upsertUserPosts(limitedPosts.map { it.toUserPostEntity() })
        postDao.trimPosts(MAX_CACHED_POSTS)
    }

    fun getPost(postId: String): Flow<Post?> {
        return combine(
            postDao.getPostWithAuthorFlow(postId),
            postStatsDao.observePostStatsEntity(postId)
        ) { post, stats ->
            post?.toDomain(stats.toDomainOrDefault())
        }
    }

    fun getCommentList(postId: String): Flow<List<Comment>> {
        return postDao.getCommentEntitiesFlow(postId).map { commentEntities ->
            assembleComments(postId, commentEntities)
        }
    }

    suspend fun getCommentPage(postId: String, limit: Int = 50, beforeTime: Long): List<Comment> {
        val safeLimit = limit.coerceAtLeast(1)
        val safeBeforeTime = if (beforeTime <= 0L) {
            System.currentTimeMillis() + 1L
        } else {
            beforeTime
        }
        return assembleComments(
            postId = postId,
            commentEntities = postDao.getCommentEntitiesPage(postId, safeLimit, safeBeforeTime)
        )
    }

    suspend fun getReplyPage(commentId: Long, limit: Int = MAX_CACHED_REPLIES_PER_COMMENT): List<Reply> {
        return postDao.getReplyEntitiesByCommentId(commentId)
            .take(limit.coerceAtLeast(1))
            .map { it.toDomain() }
    }

    fun isPostLiked(postId: String): Flow<Boolean> {
        return userRepository.currentUserId.flatMapLatest { currentUserId ->
            if (currentUserId == null || postId.isBlank()) {
                flowOf(false)
            } else {
                userRelationDao.isPostLikedFlow(currentUserId, postId)
            }
        }
    }

    suspend fun deletePost(postId: String) {
        postDao.deleteById(postId)
    }

    suspend fun addComment(postId: String, comment: Comment): List<Comment> {
        return commentCacheMutex.withLock {
            postDao.mergeCachedComments(
                postId = postId,
                incomingComments = listOf(comment.toEntity(postId)),
                limit = MAX_CACHED_COMMENTS_PER_POST
            )
            syncCommentCount(postId)
            getStoredComments(postId)
        }
    }

    suspend fun addReply(postId: String, parentCommentId: Long, reply: Reply): List<Comment> {
        return replyCacheMutex.withLock {
            postDao.mergeCachedReplies(
                commentId = parentCommentId,
                incomingReplies = listOf(reply.copy(parentCommentId = parentCommentId).toEntity(postId)),
                limit = MAX_CACHED_REPLIES_PER_COMMENT
            )
            getStoredComments(postId)
        }
    }

    suspend fun cacheComments(postId: String, comments: List<Comment>) {
        commentCacheMutex.withLock {
            postDao.mergeCachedComments(
                postId = postId,
                incomingComments = comments.map { it.toEntity(postId) },
                limit = MAX_CACHED_COMMENTS_PER_POST
            )
            syncCommentCount(postId)
        }
    }

    suspend fun cacheReplies(postId: String, commentId: Long, replies: List<Reply>) {
        replyCacheMutex.withLock {
            postDao.mergeCachedReplies(
                commentId = commentId,
                incomingReplies = replies.map { reply ->
                    reply.copy(parentCommentId = commentId).toEntity(postId)
                },
                limit = MAX_CACHED_REPLIES_PER_COMMENT
            )
        }
    }

    suspend fun setPostLiked(postId: String, liked: Boolean): Boolean {
        val currentUserId = userRepository.currentUserId.value ?: return false
        if (postId.isBlank()) return false

        val currentlyLiked = userRelationDao.isPostLiked(currentUserId, postId)
        if (currentlyLiked == liked) return liked

        postDao.getPostEntity(postId) ?: return currentlyLiked
        val postStats = postStatsDao.getPostStatsEntity(postId) ?: PostStatsEntity(postId = postId)
        val updatedLikeCount = if (liked) {
            postStats.likeCount + 1
        } else {
            (postStats.likeCount - 1).coerceAtLeast(0)
        }

        postStatsDao.upsert(postStats.copy(likeCount = updatedLikeCount))
        if (liked) {
            userRelationDao.insertLikedPost(UserLikedPostEntity(userId = currentUserId, postId = postId))
        } else {
            userRelationDao.deleteLikedPost(currentUserId, postId)
        }
        return liked
    }

    suspend fun publishPost(
        content: String,
        images: List<String> = emptyList(),
        categoryKey: String = "",
        customTags: List<String> = emptyList()
    ): Post {
        val trimmedContent = content.trim()
        require(trimmedContent.isNotBlank()) { "Post content cannot be blank." }

        val currentUser = userRepository.currentUser.value
        val now = System.currentTimeMillis()
        val post = Post(
            postId = "POST_$now",
            content = trimmedContent,
            images = images.filter { it.isNotBlank() },
            categoryKey = categoryKey,
            customTags = customTags.filter { it.isNotBlank() },
            createTime = now,
            author = PostAuthor(
                authorId = currentUser?.id ?: now,
                authorName = currentUser?.name.orEmpty().ifBlank { "匿名用户" },
                authorAvatarUrl = currentUser?.headPic.orEmpty()
            ),
            likeCount = 0,
            commentCount = 0
        )
        savePost(post)
        return post
    }

    private fun observePostList(postFlow: Flow<List<PostWithAuthorEntity>>): Flow<List<Post>> {
        return combine(postFlow, postStatsDao.observeAllPostStatsEntities()) { posts, stats ->
            val statsByPostId = stats.associateBy { it.postId }
            posts.map { post ->
                post.toDomain(statsByPostId[post.postId].toDomainOrDefault())
            }
        }
    }

    private suspend fun syncCommentCount(postId: String) {
        val currentStats = postStatsDao.getPostStatsEntity(postId) ?: PostStatsEntity(postId = postId)
        postStatsDao.upsert(currentStats.copy(commentCount = postDao.getCommentEntities(postId).size))
    }

    private suspend fun getStoredComments(postId: String): List<Comment> {
        return assembleComments(postId, postDao.getCommentEntities(postId))
    }

    private suspend fun assembleComments(postId: String, commentEntities: List<PostCommentEntity>): List<Comment> {
        if (commentEntities.isEmpty()) return emptyList()

        val repliesByCommentId = postDao
            .getReplyEntitiesByCommentIds(
                postId = postId,
                commentIds = commentEntities.map { it.commentId }
            )
            .map { it.toDomain() }
            .groupBy { it.parentCommentId }

        return commentEntities.map { comment ->
            val replies = repliesByCommentId[comment.commentId]
                .orEmpty()
                .sortedByDescending { it.time }
                .take(MAX_CACHED_REPLIES_PER_COMMENT)
            comment.toDomain(replies)
        }
    }

    private fun PostStatsEntity?.toDomainOrDefault(): PostStats {
        return this?.toDomain() ?: PostStats()
    }
}
