package com.example.grabthisforme.activity.communityFragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.communityFragment.model.CommunityFeedArgs
import com.example.grabthisforme.activity.communityFragment.model.CommunityTabMode
import com.example.grabthisforme.activity.communityFragment.ui_model.PostCardUiModel
import com.example.grabthisforme.activity.communityFragment.ui_model.toPostCardUiModel
import com.example.grabthisforme.model.location.data.AmapLocationProvider
import com.example.grabthisforme.model.location.domain.AppLocation
import com.example.grabthisforme.model.post.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val amapLocationProvider: AmapLocationProvider,
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

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> get() = _refreshing

    private var hasMore = true
    private var isLoading = false
    private var nextBeforeTime = System.currentTimeMillis() + 1L
    private var lastNearbyLocation: AppLocation? = null

    init {
        loadInitial()
    }

    fun loadMore() {
        if (tabMode == CommunityTabMode.NEARBY || isLoading || !hasMore) return
        requestPage(reset = false, fromUserRefresh = false)
    }

    fun refreshFeed() {
        when (tabMode) {
            CommunityTabMode.NEARBY -> loadNearbyFeed(fromUserRefresh = true)
            CommunityTabMode.LATEST,
            CommunityTabMode.CATEGORY -> requestPage(reset = true, fromUserRefresh = true)
        }
    }

    fun loadNearbyFeed() {
        loadNearbyFeed(fromUserRefresh = false)
    }

    fun onNearbyPermissionDenied() {
        if (tabMode != CommunityTabMode.NEARBY) return
        _postList.value = emptyList()
        _emptyMessage.value = "请开启定位权限后再查看附近帖子"
        _emptyVisible.value = true
        _initialLoading.value = false
        _refreshing.value = false
        hasMore = false
    }

    private fun loadInitial() {
        if (tabMode == CommunityTabMode.NEARBY) {
            _postList.value = emptyList()
            _emptyMessage.value = "附近页面需要先获取定位权限"
            _emptyVisible.value = true
            _initialLoading.value = false
            hasMore = false
            return
        }
        requestPage(reset = true, fromUserRefresh = false)
    }

    private fun loadNearbyFeed(fromUserRefresh: Boolean) {
        if (tabMode != CommunityTabMode.NEARBY || isLoading) return
        isLoading = true
        if (fromUserRefresh) {
            _refreshing.value = true
        } else {
            _initialLoading.value = true
        }
        _emptyVisible.value = false
        viewModelScope.launch {
            amapLocationProvider.getCurrentLocation()
                .onSuccess { location ->
                    lastNearbyLocation = location
                    _postList.value = emptyList()
                    _emptyMessage.value = buildNearbyReadyMessage(location)
                    _emptyVisible.value = true
                    hasMore = false
                }
                .onFailure { error ->
                    _postList.value = emptyList()
                    _emptyMessage.value = error.message ?: "定位失败，请稍后重试"
                    _emptyVisible.value = true
                    hasMore = false
                }
            _initialLoading.value = false
            _refreshing.value = false
            isLoading = false
        }
    }

    private fun requestPage(reset: Boolean, fromUserRefresh: Boolean) {
        if (isLoading) return
        isLoading = true
        if (reset) {
            if (fromUserRefresh) {
                _refreshing.value = true
            } else {
                _initialLoading.value = true
            }
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
            val currentItems = if (reset) emptyList() else _postList.value.orEmpty()
            val mergedItems = (currentItems + newItems).distinctBy { it.postId }
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
            _refreshing.value = false
            isLoading = false
        }
    }

    private fun buildEmptyMessage(): String {
        return when (tabMode) {
            CommunityTabMode.LATEST -> "还没有可显示的帖子"
            CommunityTabMode.NEARBY -> "附近帖子暂不可用"
            CommunityTabMode.CATEGORY -> "当前没有“$tabTitle”分类的帖子"
        }
    }

    private fun buildNearbyReadyMessage(location: AppLocation): String {
        lastNearbyLocation = location
        val targetArea = location.displayText
        return "已获取你在“$targetArea”的位置。\n附近帖子还需要后端接入帖子坐标存储与距离筛选后才能展示。"
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}
