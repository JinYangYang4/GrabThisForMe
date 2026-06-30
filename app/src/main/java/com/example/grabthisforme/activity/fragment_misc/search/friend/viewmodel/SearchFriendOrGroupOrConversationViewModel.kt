package com.example.grabthisforme.activity.fragment_misc.search.friend.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.search.friend.ui_model.SearchContactResultUiModel
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchDao
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.network.dto.GroupDto
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryRepository
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryState
import com.example.grabthisforme.model.friendAndGroup.data.repository.FriendAndGroupRemoteRepository
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import com.example.grabthisforme.model.user.data.repository.UserRemoteRepository
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SearchFriendOrGroupOrConversationViewModel @Inject constructor(
    private val searchDao: SearchDao,
    private val conversationRepository: ConversationRepository,
    private val contactDirectoryRepository: ContactDirectoryRepository,
    private val friendAndGroupRemoteRepository: FriendAndGroupRemoteRepository,
    private val userRemoteRepository: UserRemoteRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()
    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList

    private val _isExpanded = MutableLiveData(false)
    val isExpanded: LiveData<Boolean> = _isExpanded

    private val _expandLabel = MutableLiveData("展开")
    val expandLabel: LiveData<String> get() = _expandLabel

    private val _deleteMode = MutableLiveData(false)
    val deleteMode: LiveData<Boolean> get() = _deleteMode

    private val _historyEmpty = MutableLiveData(true)
    val historyEmpty: LiveData<Boolean> get() = _historyEmpty

    private val _searchInput = MutableLiveData("")
    val searchInput: LiveData<String> get() = _searchInput

    private val _searchResultList = MutableLiveData<List<SearchContactResultUiModel>>(emptyList())
    val searchResultList: LiveData<List<SearchContactResultUiModel>> get() = _searchResultList

    private val _searchResultVisible = MutableLiveData(false)
    val searchResultVisible: LiveData<Boolean> get() = _searchResultVisible

    private val _searchResultEmpty = MutableLiveData(false)
    val searchResultEmpty: LiveData<Boolean> get() = _searchResultEmpty

    private val _searchResultExpanded = MutableLiveData(false)
    val searchResultExpanded: LiveData<Boolean> get() = _searchResultExpanded

    private val _searchResultExpandVisible = MutableLiveData(false)
    val searchResultExpandVisible: LiveData<Boolean> get() = _searchResultExpandVisible

    private val _searchResultExpandLabel = MutableLiveData("展开更多")
    val searchResultExpandLabel: LiveData<String> get() = _searchResultExpandLabel

    private val _searchResultSummary = MutableLiveData("支持按联系人昵称、账号、群名或 ID 搜索")
    val searchResultSummary: LiveData<String> get() = _searchResultSummary

    private val _openConversationId = MutableLiveData<String?>(null)
    val openConversationId: LiveData<String?> get() = _openConversationId

    private val _openUserDetailId = MutableLiveData<Long?>(null)
    val openUserDetailId: LiveData<Long?> get() = _openUserDetailId

    private val _openGroupDetailId = MutableLiveData<Long?>(null)
    val openGroupDetailId: LiveData<Long?> get() = _openGroupDetailId

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    var fullList: MutableList<SearchContent> = mutableListOf()
    var limitedList: MutableList<SearchContent> = mutableListOf()

    private var currentMatchedTargets: List<SearchTargetSource> = emptyList()
    private var remoteSearchJob: Job? = null
    private var lastRemoteKeyword: String = ""
    private var lastRemoteUsers: List<User> = emptyList()
    private var lastRemoteGroups: List<Group> = emptyList()
    private var lastUserSearchSucceeded = false
    private var lastGroupSearchSucceeded = false

    init {
        viewModelScope.launch {
            contactDirectoryRepository.directoryState.collectLatest {
                refreshSearchResults()
            }
        }
    }

    fun refreshLimitedList() {
        limitedList = if (_isExpanded.value == true) {
            fullList
        } else {
            if (fullList.size > HISTORY_COLLAPSED_LIMIT) {
                fullList.take(HISTORY_COLLAPSED_LIMIT).toMutableList()
            } else {
                fullList.toMutableList()
            }
        }
        _searchHistoryList.postValue(limitedList)
        _historyEmpty.postValue(limitedList.isEmpty())
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            fullList = searchDao.getSearchByType(SearchContent.SearchType.FRIEND).first().toMutableList()
            refreshLimitedList()
        }
    }

    fun setExpand(isExpand: Boolean) {
        _isExpanded.value = isExpand
        _expandLabel.value = if (isExpand) "收起" else "展开"
    }

    fun addSearchHistory(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val newItem = SearchContent(
                search_time = currentTime,
                content = content,
                searchType = SearchContent.SearchType.FRIEND
            )

            searchDao.deleteByTypeAndContent(SearchContent.SearchType.FRIEND, content)
            searchDao.insertSearchContent(newItem)

            fullList.removeIf { it.content == content }
            fullList.add(0, newItem)
            refreshLimitedList()
        }
    }

    fun deleteByContent(content: String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.FRIEND, content)
            fullList.removeIf { it.content == content }
            refreshLimitedList()
        }
    }

    fun deleteHistory(content: String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.FRIEND, content)
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            searchDao.clearByType(SearchContent.SearchType.FRIEND)
            fullList.clear()
        }
        _searchHistoryList.postValue(mutableListOf())
        _historyEmpty.postValue(true)
    }

    fun setDeleteMode(enabled: Boolean) {
        _deleteMode.value = enabled
    }

    fun updateSearchInput(content: String) {
        _searchInput.value = content
        val keyword = content.trim()
        if (keyword.isBlank()) {
            remoteSearchJob?.cancel()
            clearRemoteSearchSnapshot()
            resetSearchResults()
            return
        }
        _searchResultExpanded.value = false
        _searchResultExpandLabel.value = "展开更多"
        _searchResultVisible.value = true
        _searchResultEmpty.value = false
        _searchResultSummary.value = "正在从服务器搜索联系人和群聊..."
        requestRemoteSearch(keyword)
    }

    fun clearSearchInput() {
        remoteSearchJob?.cancel()
        clearRemoteSearchSnapshot()
        _searchInput.value = ""
        resetSearchResults()
    }

    fun toggleSearchResultExpanded() {
        val newExpanded = !(_searchResultExpanded.value ?: false)
        _searchResultExpanded.value = newExpanded
        _searchResultExpandLabel.value = if (newExpanded) "收起结果" else "展开更多"
        refreshSearchResults()
    }

    fun onSearchResultClicked(stableId: String) {
        val target = currentMatchedTargets.firstOrNull { it.stableId == stableId } ?: return
        when (target.type) {
            SearchTargetType.FRIEND -> _openUserDetailId.value = target.id
            SearchTargetType.GROUP -> _openGroupDetailId.value = target.id
        }
    }

    fun onSearchResultActionClicked(stableId: String) {
        addSearchHistory(_searchInput.value?.trim().orEmpty())
        val target = currentMatchedTargets.firstOrNull { it.stableId == stableId } ?: return
        when (target.type) {
            SearchTargetType.FRIEND -> contactDirectoryRepository.addFriend(target.id)
            SearchTargetType.GROUP -> contactDirectoryRepository.joinGroup(target.id)
        }
    }

    fun onConversationNavigationConsumed() {
        _openConversationId.value = null
    }

    fun onUserDetailNavigationConsumed() {
        _openUserDetailId.value = null
    }

    fun onGroupDetailNavigationConsumed() {
        _openGroupDetailId.value = null
    }

    fun onErrorMessageConsumed() {
        _errorMessage.value = null
    }

    private fun requestRemoteSearch(keyword: String) {
        remoteSearchJob?.cancel()
        remoteSearchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            val usersDeferred = async { userRemoteRepository.searchUsers(keyword) }
            val groupsDeferred = async { friendAndGroupRemoteRepository.searchGroups(keyword) }

            val userResult = usersDeferred.await()
            val groupResult = groupsDeferred.await()
            if (_searchInput.value?.trim() != keyword) return@launch

            val currentUserId = userRepository.currentUserId.value
            lastRemoteKeyword = keyword
            lastUserSearchSucceeded = userResult.isSuccess
            lastGroupSearchSucceeded = groupResult.isSuccess
            lastRemoteUsers = userResult.getOrNull()
                .orEmpty()
                .map { userDto -> userDto.toDomain() }
                .filter { user -> user.id != currentUserId }
            lastRemoteGroups = groupResult.getOrNull()
                .orEmpty()
                .map { groupDto -> groupDto.toDomain() }

            if (userResult.isFailure && groupResult.isFailure) {
                _errorMessage.postValue("远程搜索失败，已回退到本地好友和已加入群聊")
            }
            refreshSearchResults()
        }
    }

    private fun refreshSearchResults() {
        val keyword = _searchInput.value?.trim().orEmpty()
        if (keyword.isBlank()) {
            resetSearchResults()
            return
        }

        val directoryState = contactDirectoryRepository.directoryState.value
        val searchTargets = buildSearchTargets(keyword, directoryState)
        currentMatchedTargets = searchTargets
            .mapNotNull { target ->
                val score = target.matchScore(keyword)
                if (score <= 0) null else target to score
            }
            .sortedWith(
                compareByDescending<Pair<SearchTargetSource, Int>> { it.second }
                    .thenByDescending { it.first.isConnected }
                    .thenBy { it.first.sortOrder }
            )
            .map { it.first }

        val displayLimit = if (_searchResultExpanded.value == true) {
            SEARCH_RESULT_EXPANDED_LIMIT
        } else {
            SEARCH_RESULT_COLLAPSED_LIMIT
        }
        val limitedTargets = currentMatchedTargets.take(displayLimit)
        _searchResultList.value = limitedTargets.map { it.toUiModel() }
        _searchResultVisible.value = true
        _searchResultEmpty.value = limitedTargets.isEmpty()
        _searchResultExpandVisible.value = currentMatchedTargets.size > SEARCH_RESULT_COLLAPSED_LIMIT
        _searchResultExpandLabel.value = if (_searchResultExpanded.value == true) "收起结果" else "展开更多"
        _searchResultSummary.value = buildSearchSummary()
    }

    private fun buildSearchTargets(
        keyword: String,
        directoryState: ContactDirectoryState
    ): List<SearchTargetSource> {
        val useRemoteUsers = lastRemoteKeyword == keyword && lastUserSearchSucceeded
        val useRemoteGroups = lastRemoteKeyword == keyword && lastGroupSearchSucceeded

        val userTargets = if (useRemoteUsers) {
            buildUserTargets(lastRemoteUsers, directoryState)
        } else {
            buildFriendTargets(directoryState.friends, directoryState)
        }

        val groupTargets = if (useRemoteGroups) {
            buildGroupTargets(lastRemoteGroups, directoryState)
        } else {
            buildGroupTargets(directoryState.groups, directoryState)
        }
        return userTargets + groupTargets
    }

    private fun buildSearchSummary(): String {
        return when {
            currentMatchedTargets.isEmpty() -> "没有找到相关联系人或群聊，请尝试昵称、账号、群名或 ID"
            currentMatchedTargets.size > SEARCH_RESULT_EXPANDED_LIMIT -> "匹配结果较多，最多展开显示前 $SEARCH_RESULT_EXPANDED_LIMIT 条"
            lastUserSearchSucceeded || lastGroupSearchSucceeded -> "已优先显示服务器搜索结果，共找到 ${currentMatchedTargets.size} 条"
            else -> "服务器搜索失败，当前显示本地好友和已加入群聊，共 ${currentMatchedTargets.size} 条"
        }
    }

    private fun resetSearchResults() {
        _searchResultVisible.value = false
        _searchResultEmpty.value = false
        _searchResultExpanded.value = false
        _searchResultExpandVisible.value = false
        _searchResultExpandLabel.value = "展开更多"
        _searchResultSummary.value = "支持按联系人昵称、账号、群名或 ID 搜索"
        _searchResultList.value = emptyList()
        currentMatchedTargets = emptyList()
    }

    private fun clearRemoteSearchSnapshot() {
        lastRemoteKeyword = ""
        lastRemoteUsers = emptyList()
        lastRemoteGroups = emptyList()
        lastUserSearchSucceeded = false
        lastGroupSearchSucceeded = false
    }

    private fun openConversation(target: SearchTargetSource) {
        viewModelScope.launch {
            val result = when (target.type) {
                SearchTargetType.FRIEND -> {
                    val peerUser = resolveUser(target.id) ?: return@launch
                    conversationRepository.findOrCreateSingleConversation(peerUser = peerUser)
                }

                SearchTargetType.GROUP -> {
                    val group = resolveGroup(target.id) ?: return@launch
                    conversationRepository.findOrCreateGroupConversation(
                        groupId = target.id,
                        members = group.members
                    )
                }
            }
            result.onSuccess { conversation ->
                _openConversationId.postValue(conversation.conversationId)
                Log.d("SearchContactViewModel", "open conversation success")
            }.onFailure { throwable ->
                Log.e("SearchContactViewModel", "open conversation failed", throwable)
                _errorMessage.postValue("打开会话失败")
            }
        }
    }

    private fun resolveUser(userId: Long): User? {
        return contactDirectoryRepository.directoryState.value.findFriend(userId)?.who
            ?: lastRemoteUsers.firstOrNull { user -> user.id == userId }
    }

    private fun resolveGroup(groupId: Long): Group? {
        return contactDirectoryRepository.directoryState.value.findGroup(groupId)
            ?: lastRemoteGroups.firstOrNull { group -> group.groupId == groupId }
    }

    private data class SearchTargetSource(
        val stableId: String,
        val id: Long,
        val type: SearchTargetType,
        val title: String,
        val subtitle: String,
        val searchKeywords: List<String>,
        val isConnected: Boolean,
        val sortOrder: Int
    ) {
        fun matchScore(keyword: String): Int {
            val trimmedKeyword = keyword.trim()
            if (trimmedKeyword.isBlank()) return 0

            var score = 0
            if (title.contains(trimmedKeyword, ignoreCase = true)) {
                score += 100
            }
            if (subtitle.contains(trimmedKeyword, ignoreCase = true)) {
                score += 50
            }
            if (searchKeywords.any { it == trimmedKeyword }) {
                score += 160
            } else if (searchKeywords.any { it.startsWith(trimmedKeyword, ignoreCase = true) }) {
                score += 90
            } else if (searchKeywords.any { it.contains(trimmedKeyword, ignoreCase = true) }) {
                score += 36
            }
            if (isConnected) {
                score += 20
            }
            if (type == SearchTargetType.FRIEND) {
                score += 6
            }
            return score
        }

        fun toUiModel(): SearchContactResultUiModel {
            return when (type) {
                SearchTargetType.FRIEND -> SearchContactResultUiModel(
                    stableId = stableId,
                    title = title,
                    subtitle = subtitle,
                    badgeText = "联系人",
                    statusText = if (isConnected) "已添加，可直接进入资料或发起聊天" else "尚未添加，可先发起好友申请",
                    actionText = if (isConnected) null else "添加好友",
                    isFriend = true,
                    isConnected = isConnected
                )

                SearchTargetType.GROUP -> SearchContactResultUiModel(
                    stableId = stableId,
                    title = title,
                    subtitle = subtitle,
                    badgeText = "群聊",
                    statusText = if (isConnected) "已加入，可直接进入群详情" else "尚未加入，可先加入群聊",
                    actionText = if (isConnected) null else "加入群聊",
                    isFriend = false,
                    isConnected = isConnected
                )
            }
        }
    }

    private enum class SearchTargetType {
        FRIEND,
        GROUP
    }

    companion object {
        private const val HISTORY_COLLAPSED_LIMIT = 10
        private const val SEARCH_RESULT_COLLAPSED_LIMIT = 15
        private const val SEARCH_RESULT_EXPANDED_LIMIT = 50
        private const val SEARCH_DEBOUNCE_MILLIS = 300L

        private fun buildFriendTargets(
            friends: List<Friend>,
            directoryState: ContactDirectoryState
        ): List<SearchTargetSource> {
            return friends.mapIndexed { index, friend ->
                friend.who.toSearchTargetSource(
                    isConnected = directoryState.isFriendConnected(friend.friendId),
                    sortOrder = index
                )
            }
        }

        private fun buildUserTargets(
            users: List<User>,
            directoryState: ContactDirectoryState
        ): List<SearchTargetSource> {
            return users.mapIndexed { index, user ->
                user.toSearchTargetSource(
                    isConnected = directoryState.isFriendConnected(user.id),
                    sortOrder = index
                )
            }
        }

        private fun User.toSearchTargetSource(
            isConnected: Boolean,
            sortOrder: Int
        ): SearchTargetSource {
            val userIdText = id.toString()
            return SearchTargetSource(
                stableId = "friend_$id",
                id = id,
                type = SearchTargetType.FRIEND,
                title = name,
                subtitle = buildFriendSubtitle(userIdText = userIdText, signature = signature),
                searchKeywords = buildList {
                    add(name)
                    add(signature ?: "")
                    add(phone ?: "")
                    add(accountName)
                    add(userIdText)
                    add("id$userIdText")
                    add("ID$userIdText")
                }.filter { it.isNotBlank() },
                isConnected = isConnected,
                sortOrder = sortOrder
            )
        }

        private fun buildGroupTargets(
            groups: List<Group>,
            directoryState: ContactDirectoryState
        ): List<SearchTargetSource> {
            return groups.mapIndexed { index, group ->
                val groupIdText = group.groupId.toString()
                SearchTargetSource(
                    stableId = "group_${group.groupId}",
                    id = group.groupId,
                    type = SearchTargetType.GROUP,
                    title = group.groupName,
                    subtitle = "群 ID $groupIdText · ${group.members.size} 位成员",
                    searchKeywords = buildList {
                        add(group.groupName)
                        add(groupIdText)
                        add("id$groupIdText")
                        add("ID$groupIdText")
                        group.members.take(5).forEach { member -> add(member.name) }
                    }.filter { it.isNotBlank() },
                    isConnected = directoryState.isGroupJoined(group.groupId),
                    sortOrder = 100 + index
                )
            }
        }

        private fun GroupDto.toDomain(): Group {
            val members = members.map { memberDto ->
                memberDto.user?.toDomain() ?: User(
                    id = memberDto.userId,
                    name = memberDto.userId.toString(),
                    headPic = "",
                    accountName = memberDto.userId.toString()
                )
            }
            return Group(
                groupId = groupId,
                groupName = groupName,
                members = members,
                createTime = createTime
            )
        }

        private fun buildFriendSubtitle(
            userIdText: String,
            signature: String?
        ): String {
            val normalizedSignature = signature?.takeIf { it.isNotBlank() } ?: "校园互助用户"
            return "ID $userIdText · $normalizedSignature"
        }
    }
}
