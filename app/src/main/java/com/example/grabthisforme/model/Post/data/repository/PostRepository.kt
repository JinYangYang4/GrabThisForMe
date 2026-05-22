package com.example.grabthisforme.model.post.data.repository

import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.model.post.data.dao.PostDao
import com.example.grabthisforme.model.post.data.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.mock.PostMockData
import com.example.grabthisforme.model.post.data.mock.PostCommentMockData
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.domain.PostAuthor
import com.example.grabthisforme.model.post.mapper.toCommentList
import com.example.grabthisforme.model.post.mapper.toCommentsJson
import com.example.grabthisforme.model.user.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val userRepository: UserRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sourcePosts: StateFlow<List<Post>> = postDao.getAllPosts()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allPostList: StateFlow<List<Post>> = sourcePosts

    val myPostList: StateFlow<List<Post>> = combine(
        sourcePosts,
        userRepository.currentUserId
    ) { posts, currentUserId ->
        if (currentUserId == null) {
            emptyList()
        } else {
            posts.filter { it.authorId == currentUserId }
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        repositoryScope.launch {
            val cachedPosts = postDao.getAllPosts().first()
            if (cachedPosts.isEmpty()) {
                val mockPosts = PostMockData.getPostList()
                postDao.savePosts(mockPosts)
                postDao.insertCommentsIfAbsent(
                    mockPosts.map { post ->
                        PostCommentEntity(
                            postId = post.postId,
                            commentsJson = PostCommentMockData.buildCommentList(post.commentCount)
                                .toCommentsJson()
                        )
                    }
                )
            }
        }
    }

    suspend fun savePost(post: Post) {
        postDao.savePost(post)
        postDao.insertCommentIfAbsent(PostCommentEntity(postId = post.postId))
    }

    suspend fun savePosts(posts: List<Post>) {
        postDao.savePosts(posts)
        postDao.insertCommentsIfAbsent(
            posts.map { PostCommentEntity(postId = it.postId) }
        )
    }

    fun getPost(postId: String): Flow<Post?> {
        return postDao.getPost(postId)
    }

    fun getCommentList(postId: String): Flow<List<Comment>> {
        return postDao.getPostCommentEntityFlow(postId).map { entity ->
            entity?.commentsJson?.toCommentList().orEmpty()
        }
    }

    suspend fun getCommentListOnce(postId: String): List<Comment> {
        return getStoredComments(postId)
    }

    fun isPostLiked(postId: String): Flow<Boolean> {
        return userRepository.currentUser.map { user ->
            user?.likedPostIds?.contains(postId) == true
        }
    }

    suspend fun deletePost(postId: String) {
        postDao.deleteById(postId)
    }

    suspend fun addComment(postId: String, comment: Comment): List<Comment> {
        val updatedComments = listOf(comment) + getStoredComments(postId)
        saveComments(postId, updatedComments)
        return updatedComments
    }

    suspend fun addReply(
        postId: String,
        parentCommentId: Long,
        reply: Reply
    ): List<Comment> {
        val currentComments = getStoredComments(postId)
        val updatedComments = currentComments.map { comment ->
            if (comment.id == parentCommentId) {
                comment.copy(replies = listOf(reply) + comment.replies)
            } else {
                comment
            }
        }
        saveComments(postId, updatedComments)
        return updatedComments
    }

    suspend fun setPostLiked(postId: String, liked: Boolean): Boolean {
        val currentUser = userRepository.currentUser.value ?: return false
        val currentlyLiked = currentUser.likes.hasLikedPost(postId)
        if (currentlyLiked == liked) return liked

        val postEntity = postDao.getPostEntity(postId) ?: return currentlyLiked
        val updatedLikeCount = if (liked) {
            postEntity.likeCount + 1
        } else {
            (postEntity.likeCount - 1).coerceAtLeast(0)
        }

        postDao.upsert(postEntity.copy(likeCount = updatedLikeCount))
        userRepository.setPostLiked(postId, liked)
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
                authorName = currentUser?.name.orEmpty().ifBlank { "GuestUser" },
                authorAvatarUrl = currentUser?.headPic.orEmpty()
            ),
            likeCount = 0,
            commentCount = 0
        )
        postDao.savePost(post)
        postDao.insertCommentIfAbsent(PostCommentEntity(postId = post.postId))
        return post
    }

    private suspend fun getStoredComments(postId: String): List<Comment> {
        return postDao.getPostCommentEntity(postId)?.commentsJson?.toCommentList().orEmpty()
    }

    private suspend fun saveComments(postId: String, comments: List<Comment>) {
        postDao.savePostComments(
            postId = postId,
            commentsJson = comments.toCommentsJson(),
            commentCount = comments.size
        )
    }
}
