package com.example.grabthisforme.activity.fragment_misc.post_topic.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.post.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

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

    private val _selectedCategory = MutableLiveData(PostCategory.GOSSIP)
    val selectedCategory: LiveData<PostCategory> get() = _selectedCategory

    private val _customTags = MutableLiveData<List<String>>(emptyList())
    val customTags: LiveData<List<String>> get() = _customTags

    private val _tagSummaryText = MutableLiveData("大分类：吐槽 · 自定义标签 0/3")
    val tagSummaryText: LiveData<String> get() = _tagSummaryText

    private val _actionResult = MutableLiveData<PostTopicActionResult>()
    val actionResult: LiveData<PostTopicActionResult> get() = _actionResult

    private var draftState = PostDraftState()

    init {
        restoreDraft()
    }

    fun categories(): List<PostCategory> = PostCategory.entries

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

    fun updateCategory(category: PostCategory) {
        if (_selectedCategory.value == category) return
        _selectedCategory.value = category
        draftState = draftState.copy(category = category)
        refreshEditorState()
    }

    fun addCustomTag(input: String) {
        val normalized = input.trim()
            .replace(Regex("\\s+"), " ")
            .take(MAX_TAG_LENGTH)
        if (normalized.isBlank()) {
            _actionResult.value = PostTopicActionResult(false, "标签不能为空")
            return
        }
        val currentTags = _customTags.value.orEmpty()
        if (currentTags.any { it.equals(normalized, ignoreCase = true) }) {
            _actionResult.value = PostTopicActionResult(false, "这个标签已经添加过了")
            return
        }
        if (currentTags.size >= MAX_CUSTOM_TAG_COUNT) {
            _actionResult.value =
                PostTopicActionResult(false, "最多添加 $MAX_CUSTOM_TAG_COUNT 个自定义标签")
            return
        }
        val updatedTags = currentTags + normalized
        _customTags.value = updatedTags
        draftState = draftState.copy(customTags = updatedTags)
        refreshEditorState()
    }

    fun removeCustomTag(tag: String) {
        val updatedTags = _customTags.value.orEmpty().filterNot { it == tag }
        _customTags.value = updatedTags
        draftState = draftState.copy(customTags = updatedTags)
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
        val category = _selectedCategory.value ?: PostCategory.GOSSIP
        val customTags = _customTags.value.orEmpty()
        if (content.isBlank()) {
            _actionResult.value = PostTopicActionResult(
                success = false,
                message = "请先输入帖子正文"
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                postRepository.publishPost(
                    content = content,
                    images = images,
                    categoryKey = category.key,
                    customTags = customTags
                )
            }.onSuccess {
                draftState = PostDraftState(category = category)
                _contentText.value = ""
                _selectedImages.value = emptyList()
                _customTags.value = emptyList()
                refreshEditorState()
                _actionResult.value = PostTopicActionResult(
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
        _selectedCategory.value = draftState.category
        _customTags.value = draftState.customTags
        refreshEditorState()
    }

    private fun currentDraft(): PostDraftState {
        return PostDraftState(
            content = _contentText.value.orEmpty(),
            images = _selectedImages.value.orEmpty(),
            category = _selectedCategory.value ?: PostCategory.GOSSIP,
            customTags = _customTags.value.orEmpty()
        )
    }

    private fun refreshEditorState() {
        val content = _contentText.value.orEmpty()
        val images = _selectedImages.value.orEmpty()
        val category = _selectedCategory.value ?: PostCategory.GOSSIP
        val tags = _customTags.value.orEmpty()
        _selectedImageCount.value = images.size
        _contentLength.value = content.length
        _canPublish.value = content.trim().isNotBlank()
        _tagSummaryText.value = "大分类：${category.label} · 自定义标签 ${tags.size}/$MAX_CUSTOM_TAG_COUNT"
        _draftStatusText.value = when {
            content.isBlank() && images.isEmpty() && tags.isEmpty() -> "未开始编辑"
            content.trim().isBlank() && images.isNotEmpty() -> "已添加图片，等待补充正文"
            content.trim().isBlank() && tags.isNotEmpty() -> "已设置标签，等待补充正文"
            images.isEmpty() && tags.isEmpty() -> "正文草稿已就绪"
            images.isEmpty() -> "正文与标签已准备好"
            else -> "正文、配图和标签都已准备好"
        }
        draftState = draftState.copy(
            content = content,
            images = images,
            category = category,
            customTags = tags
        )
    }

    companion object {
        const val MAX_CUSTOM_TAG_COUNT = 3
        const val MAX_TAG_LENGTH = 10
    }
}

data class PostDraftState(
    val content: String = "",
    val images: List<String> = emptyList(),
    val category: PostCategory = PostCategory.GOSSIP,
    val customTags: List<String> = emptyList()
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

enum class PostCategory(
    val key: String,
    val label: String
) {
    FUNNY("FUNNY", "搞笑"),
    GOSSIP("GOSSIP", "吐槽"),
    SHARE("SHARE", "分享"),
    FRESH("FRESH", "新鲜"),
    SECOND_HAND("SECOND_HAND", "二手"),
    MAKE_FRIENDS("MAKE_FRIENDS", "交友"),
    GAME("GAME", "游戏"),
    LOST_FOUND("LOST_FOUND", "失物"),
    CLUB("CLUB", "社团"),
    FOOD("FOOD", "美食"),
    WARNING("WARNING", "避雷"),
    QUESTION("QUESTION", "疑问")
}
