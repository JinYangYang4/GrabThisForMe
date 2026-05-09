package com.example.grabthisforme.activity.fragment_misc.post_topic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.post.data.dao.PostDao
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.domain.PostAuthor
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostTopicViewModel @Inject constructor(
    private val postDao: PostDao,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _contentText = MutableLiveData("")
    val contentText: LiveData<String> get() = _contentText

    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> get() = _selectedImages

    private val _selectedImageCount = MutableLiveData(0)
    val selectedImageCount: LiveData<Int> get() = _selectedImageCount

    private val _canPublish = MutableLiveData(false)
    val canPublish: LiveData<Boolean> get() = _canPublish

    private val _actionResult = MutableLiveData<PostTopicActionResult>()
    val actionResult: LiveData<PostTopicActionResult> get() = _actionResult

    private var draftState = PostDraftState()

    init {
        restoreDraft()
    }

    fun updateContent(content: String) {
        _contentText.value = content
        draftState = draftState.copy(content = content)
        refreshPublishState()
    }

    fun updateSelectedImages(imageUris: List<String>) {
        val cleanedImages = imageUris.filter { it.isNotBlank() }
        _selectedImages.value = cleanedImages
        _selectedImageCount.value = cleanedImages.size
        draftState = draftState.copy(images = cleanedImages)
        refreshPublishState()
    }

    fun saveDraft() {
        draftState = currentDraft()
        _actionResult.value = PostTopicActionResult(
            success = true,
            message = "草稿已保存",
            actionType = PostTopicActionType.DRAFT_SAVED
        )
    }

    fun publishPost() {
        val content = _contentText.value.orEmpty().trim()
        val images = _selectedImages.value.orEmpty()
        if (content.isBlank()) {
            _actionResult.value = PostTopicActionResult(
                success = false,
                message = "请输入帖子正文"
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                val author = userRepository.currentUser.value ?: buildFallbackUser()
                val now = System.currentTimeMillis()
                val post = Post(
                    postId = "POST_$now",
                    content = content,
                    images = images,
                    createTime = now,
                    author = PostAuthor(
                        authorId = author.id,
                        authorName = author.name,
                        authorAvatarUrl = author.headPic
                    ),
                    likeCount = 0,
                    commentCount = 0
                )
                postDao.savePost(post)
            }.onSuccess {
                draftState = PostDraftState()
                _contentText.postValue("")
                _selectedImages.postValue(emptyList())
                _selectedImageCount.postValue(0)
                refreshPublishState()
                _actionResult.postValue(
                    PostTopicActionResult(
                        success = true,
                        message = "帖子发布成功",
                        actionType = PostTopicActionType.PUBLISHED
                    )
                )
            }.onFailure {
                _actionResult.postValue(
                    PostTopicActionResult(
                        success = false,
                        message = "帖子发布失败: ${it.message ?: "未知错误"}"
                    )
                )
            }
        }
    }

    fun getCurrentDraft(): PostDraftState = currentDraft()

    private fun restoreDraft() {
        _contentText.value = draftState.content
        _selectedImages.value = draftState.images
        _selectedImageCount.value = draftState.images.size
        refreshPublishState()
    }

    private fun currentDraft(): PostDraftState {
        return PostDraftState(
            content = _contentText.value.orEmpty(),
            images = _selectedImages.value.orEmpty()
        )
    }

    private fun refreshPublishState() {
        _canPublish.value = _contentText.value.orEmpty().trim().isNotBlank()
    }

    private fun buildFallbackUser(): User {
        val now = System.currentTimeMillis()
        return User(
            id = now,
            name = "GuestUser",
            headPic = "",
            isCurrent = true
        )
    }
}

data class PostDraftState(
    val content: String = "",
    val images: List<String> = emptyList()
)

data class PostTopicActionResult(
    val success: Boolean,
    val message: String,
    val actionType: PostTopicActionType = PostTopicActionType.NONE,
    val eventId: Long = System.currentTimeMillis()
)

enum class PostTopicActionType {
    NONE,
    DRAFT_SAVED,
    PUBLISHED
}
