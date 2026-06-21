package com.example.grabthisforme.activity.communityFragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.communityFragment.model.CommunityFeedArgs
import com.example.grabthisforme.activity.communityFragment.model.CommunityTabMode
import com.example.grabthisforme.activity.communityFragment.model.CommunityTabs
import com.example.grabthisforme.activity.communityFragment.ui_model.PostCardUiModel
import com.example.grabthisforme.activity.communityFragment.ui_model.toPostCardUiModel
import com.example.grabthisforme.model.post.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val tabTitle: String = savedStateHandle[CommunityFeedArgs.TITLE] ?: "最新"
    private val tabMode: CommunityTabMode =
        savedStateHandle.get<String>(CommunityFeedArgs.MODE)
            ?.let(CommunityTabMode::valueOf)
            ?: CommunityTabMode.LATEST
    private val categoryKey: String? = savedStateHandle[CommunityFeedArgs.CATEGORY_KEY]

    private val _postList = MutableLiveData<List<PostCardUiModel>>(emptyList())
    val postList: LiveData<List<PostCardUiModel>> get() = _postList

    private val _emptyVisible = MutableLiveData(false)
    val emptyVisible: LiveData<Boolean> get() = _emptyVisible

    private val _emptyMessage = MutableLiveData("")
    val emptyMessage: LiveData<String> get() = _emptyMessage

    private val _initialLoading = MutableLiveData(true)
    val initialLoading: LiveData<Boolean> get() = _initialLoading

    private var hasMore = true
    private var isLoading = false
    private var nextBeforeTime = System.currentTimeMillis() + 1L

    init {
        loadInitial()
    }

    fun loadMore() {
        if (tabMode == CommunityTabMode.NEARBY || isLoading || !hasMore) return
        requestPage(reset = false)
    }

    private fun loadInitial() {
        if (tabMode == CommunityTabMode.NEARBY || categoryKey == CommunityTabs.NEARBY_PLACEHOLDER_KEY) {
            _postList.value = emptyList()
            _emptyMessage.value = "附近功能建议在接入发帖地址后开放，当前暂不展示附近帖子。"
            _emptyVisible.value = true
            _initialLoading.value = false
            hasMore = false
            return
        }
        requestPage(reset = true)
    }

    private fun requestPage(reset: Boolean) {
        if (isLoading) return
        isLoading = true
        if (reset) {
            _initialLoading.value = true
            hasMore = true
            nextBeforeTime = System.currentTimeMillis() + 1L
        }
        viewModelScope.launch {
            val page = postRepository.getPostPage(
                limit = PAGE_SIZE,
                beforeTime = nextBeforeTime,
                categoryKey = if (tabMode == CommunityTabMode.CATEGORY) categoryKey else null
            )
            val newItems = page.items
                .sortedByDescending { it.createTime }
                .map { it.toPostCardUiModel() }
            val currentItems = if (reset) {
                emptyList()
            } else {
                _postList.value.orEmpty()
            }
            val mergedItems = (currentItems + newItems)
                .distinctBy { it.postId }
            _postList.value = mergedItems
            val lastCreateTime = page.items.lastOrNull()?.createTime
            if (lastCreateTime != null) {
                nextBeforeTime = lastCreateTime - 1L
            }
            hasMore = page.hasMore && newItems.isNotEmpty()
            if (mergedItems.isEmpty()) {
                _emptyMessage.value = buildEmptyMessage()
                _emptyVisible.value = true
            } else {
                _emptyVisible.value = false
            }
            _initialLoading.value = false
            isLoading = false
        }
    }

    private fun buildEmptyMessage(): String {
        return when (tabMode) {
            CommunityTabMode.LATEST -> "还没有可显示的帖子。"
            CommunityTabMode.NEARBY -> "附近功能在接入发帖地址后开放，当前暂不展示附近帖子。"
            CommunityTabMode.CATEGORY -> "当前没有“$tabTitle”分类的帖子。"
        }
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
