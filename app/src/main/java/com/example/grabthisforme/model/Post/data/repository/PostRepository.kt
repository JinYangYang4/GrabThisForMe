package com.example.grabthisforme.model.post.data.repository

import com.example.grabthisforme.model.post.data.dao.PostDao
import com.example.grabthisforme.model.post.data.mock.PostMockData
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.domain.PostAuthor
import com.example.grabthisforme.model.user.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
                postDao.savePosts(PostMockData.getPostList())
            }
        }
    }

    suspend fun savePost(post: Post) {
        postDao.savePost(post)
    }

    suspend fun savePosts(posts: List<Post>) {
        postDao.savePosts(posts)
    }

    suspend fun deletePost(postId: String) {
        postDao.deleteById(postId)
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
        return post
    }
}
