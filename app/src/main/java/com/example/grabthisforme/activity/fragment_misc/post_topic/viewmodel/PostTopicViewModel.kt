package com.example.grabthisforme.activity.fragment_misc.post_topic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.post.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostTopicViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    private val _contentText = MutableLiveData("")
    val contentText: LiveData<String> get() = _contentText

    private val _selectedImages = MutableLiveData<List<String>>(emptyList())
    val selectedImages: LiveData<List<String>> get() = _selectedImages

    private val _selectedImageCount = MutableLiveData(0)
    val selectedImageCount: LiveData<Int> get() = _selectedImageCount

    private val _contentLength = MutableLiveData(0)
    val contentLength: LiveData<Int> get() = _contentLength

    private val _draftStatusText = MutableLiveData("未开始编辑")
    val draftStatusText: LiveData<String> get() = _draftStatusText

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
        refreshEditorState()
    }

    fun updateSelectedImages(imageUris: List<String>) {
        val cleanedImages = imageUris.filter { it.isNotBlank() }
        _selectedImages.value = cleanedImages
        draftState = draftState.copy(images = cleanedImages)
        refreshEditorState()
    }

    fun saveDraft() {
        val currentDraft = currentDraft()
        if (currentDraft.content.isBlank() && currentDraft.images.isEmpty()) {
            _actionResult.value = PostTopicActionResult(
                success = false,
                message = "当前还没有可保存的内容"
            )
            return
        }
        draftState = currentDraft
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
                message = "请先输入帖子正文"
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                postRepository.publishPost(content = content, images = images)
            }.onSuccess {
                draftState = PostDraftState()
                _contentText.value = ""
                _selectedImages.value = emptyList()
                refreshEditorState()
                _actionResult.value =
                    PostTopicActionResult(
                        success = true,
                        message = "帖子发布成功",
                        actionType = PostTopicActionType.PUBLISHED
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
        refreshEditorState()
    }

    private fun currentDraft(): PostDraftState {
        return PostDraftState(
            content = _contentText.value.orEmpty(),
            images = _selectedImages.value.orEmpty()
        )
    }

    private fun refreshEditorState() {
        val content = _contentText.value.orEmpty()
        val images = _selectedImages.value.orEmpty()
        _selectedImageCount.value = images.size
        _contentLength.value = content.length
        _canPublish.value = content.trim().isNotBlank()
        _draftStatusText.value = when {
            content.isBlank() && images.isEmpty() -> "未开始编辑"
            content.trim().isBlank() && images.isNotEmpty() -> "已添加图片，待补充正文"
            images.isEmpty() -> "正文草稿已就绪"
            else -> "正文和配图都已准备"
        }
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
