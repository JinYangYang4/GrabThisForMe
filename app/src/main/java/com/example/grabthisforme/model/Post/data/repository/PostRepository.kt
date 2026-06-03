package com.example.grabthisforme.model.post.data.repository

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.data.dao.PostDao
import com.example.grabthisforme.model.post.data.dao.PostStatsDao
import com.example.grabthisforme.model.post.data.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.entity.PostStatsEntity
import com.example.grabthisforme.model.post.data.entity.PostWithAuthorEntity
import com.example.grabthisforme.model.post.data.mock.PostMockData
import com.example.grabthisforme.model.post.data.mock.PostCommentMockData
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val postStatsDao: PostStatsDao,
    private val userRelationDao: UserRelationDao,
    private val userRepository: UserRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                val mockPosts = PostMockData.getPostList()
                savePosts(mockPosts)
                mockPosts.forEach { post ->
                    val mockComments = PostCommentMockData.buildCommentList(post.commentCount)
                    postDao.replacePostComments(
                        postId = post.postId,
                        comments = mockComments.map { it.toEntity(post.postId) },
                        replies = mockComments.flatMap { comment ->
                            comment.replies.map { it.toEntity(post.postId) }
                        }
                    )
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
    }

    suspend fun savePosts(posts: List<Post>) {
        postDao.insertAuthorAccountsIfAbsent(posts.map { it.toAuthorAccountEntity() })
        postDao.insertAuthorProfilesIfAbsent(posts.map { it.toAuthorProfileEntity() })
        postDao.upsertAll(posts.map { it.toEntity() })
        postStatsDao.upsertAll(posts.map { it.toStatsEntity() })
        userRelationDao.upsertUserPosts(posts.map { it.toUserPostEntity() })
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

    suspend fun getCommentListOnce(postId: String): List<Comment> {
        return getStoredComments(postId)
    }

    suspend fun getCommentPage(postId: String, limit: Int = 50, offset: Int = 0): List<Comment> {
        val safeLimit = limit.coerceAtLeast(1)
        val safeOffset = offset.coerceAtLeast(0)
        return assembleComments(
            postId = postId,
            commentEntities = postDao.getCommentEntitiesPage(postId, safeLimit, safeOffset)
        )
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
        postDao.upsertComment(comment.toEntity(postId))
        syncCommentCount(postId)
        return getStoredComments(postId)
    }

    suspend fun addReply(
        postId: String,
        parentCommentId: Long,
        reply: Reply
    ): List<Comment> {
        postDao.upsertReply(reply.copy(parentCommentId = parentCommentId).toEntity(postId))
        return getStoredComments(postId)
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
            userRelationDao.insertLikedPost(
                UserLikedPostEntity(
                    userId = currentUserId,
                    postId = postId
                )
            )
        } else {
            userRelationDao.deleteLikedPost(currentUserId, postId)
        }
        return liked
    }

    suspend fun publishPost(
        content: String,
        images: List<String> = emptyList()
    ): Post {
        val trimmedContent = content.trim()
        require(trimmedContent.isNotBlank()) { "Post content cannot be blank." }

        val currentUser = userRepository.currentUser.value
        val now = System.currentTimeMillis()
        val post = Post(
            postId = "POST_$now",
            content = trimmedContent,
            images = images.filter { it.isNotBlank() },
            createTime = now,
            author = PostAuthor(
                authorId = currentUser?.id ?: now,
                authorName = currentUser?.name.orEmpty().ifBlank { "游客" },
                authorAvatarUrl = currentUser?.headPic.orEmpty()
            ),
            likeCount = 0,
            commentCount = 0
        )
        savePost(post)
        return post
    }

    private fun observePostList(postFlow: Flow<List<PostWithAuthorEntity>>): Flow<List<Post>> {
        return combine(
            postFlow,
            postStatsDao.observeAllPostStatsEntities()
        ) { posts, stats ->
            val statsByPostId = stats.associateBy { it.postId }
            posts.map { post ->
                post.toDomain(statsByPostId[post.postId].toDomainOrDefault())
            }
        }
    }

    private suspend fun syncCommentCount(postId: String) {
        val currentStats = postStatsDao.getPostStatsEntity(postId) ?: PostStatsEntity(postId = postId)
        postStatsDao.upsert(
            currentStats.copy(commentCount = postDao.getCommentEntities(postId).size)
        )
    }

    private suspend fun getStoredComments(postId: String): List<Comment> {
        return assembleComments(postId, postDao.getCommentEntities(postId))
    }

    private suspend fun assembleComments(
        postId: String,
        commentEntities: List<PostCommentEntity>
    ): List<Comment> {
        if (commentEntities.isEmpty()) return emptyList()

        val repliesByCommentId = postDao
            .getReplyEntitiesByCommentIds(
                postId = postId,
                commentIds = commentEntities.map { it.commentId }
            )
            .map { it.toDomain() }
            .groupBy { it.parentCommentId }

        return commentEntities.map { comment ->
            comment.toDomain(repliesByCommentId[comment.commentId].orEmpty())
        }
    }

    private fun PostStatsEntity?.toDomainOrDefault(): PostStats {
        return this?.toDomain() ?: PostStats()
    }
}
