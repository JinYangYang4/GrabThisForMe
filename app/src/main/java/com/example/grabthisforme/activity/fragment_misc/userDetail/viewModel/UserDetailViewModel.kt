package com.example.grabthisforme.activity.fragment_misc.userDetail.viewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.userDetail.ui_model.UserCommonGroupItemUiModel
import com.example.grabthisforme.activity.fragment_misc.userDetail.ui_model.UserDetailUiModel
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryRepository
import com.example.grabthisforme.model.user.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactDirectoryRepository: ContactDirectoryRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val userId: Long = savedStateHandle["userId"] ?: -1L

    private val _uiModel = MutableLiveData<UserDetailUiModel>()
    val uiModel: LiveData<UserDetailUiModel> get() = _uiModel

    private val _commonGroups = MutableLiveData<List<UserCommonGroupItemUiModel>>(emptyList())
    val commonGroups: LiveData<List<UserCommonGroupItemUiModel>> get() = _commonGroups

    private val _openConversationId = MutableLiveData<String?>(null)
    val openConversationId: LiveData<String?> get() = _openConversationId

    private val _openGroupDetailId = MutableLiveData<Long?>(null)
    val openGroupDetailId: LiveData<Long?> get() = _openGroupDetailId

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> get() = _errorMessage

    init {
        viewModelScope.launch {
            contactDirectoryRepository.directoryState.collectLatest { state ->
                val friend = state.findFriend(userId) ?: state.findPendingFriendRequest(userId) ?: return@collectLatest
                val isConnected = state.isFriendConnected(userId)
                _uiModel.value = UserDetailUiModel(
                    userId = friend.friendId,
                    name = friend.who.name,
                    signature = friend.who.signature ?: "这个人很低调，但在校园生活里很靠谱",
                    phoneText = "电话：${friend.who.phone ?: "未公开"}",
                    genderText = "性别：" + when (friend.who.gender) {
                        UserProfile.GENDER_MALE -> "鐢风敓"
                        UserProfile.GENDER_FEMALE -> "濂崇敓"
                        else -> "未设置"
                    },
                    statusText = if (isConnected) {
                        "已是联系人，可随时发消息"
                    } else {
                        "还不是联系人，可先添加再聊天"
                    },
                    primaryActionText = if (isConnected) "删除好友" else "添加好友",
                    secondaryActionText = if (isConnected) "发消息" else "临时会话",
                    isConnected = isConnected,
                    accountHint = "璐﹀彿 ${friend.who.accountName}",
                    campusHint = "适合拼单、跑腿、闲置交换这类校园轻社交场景",
                    activityHint = "最近常出现在生活区、快递站和校内互助群里",
                    groupSummary = "你们有 ${state.commonGroupsForUser(userId).size} 个共同群聊"
                )
                _commonGroups.value = state.commonGroupsForUser(userId).map { group ->
                    UserCommonGroupItemUiModel(
                        groupId = group.groupId,
                        title = group.groupName,
                        subtitle = "${group.members.size} 位成员"
                    )
                }
            }
        }
    }

    fun onPrimaryActionClick() {
        val state = contactDirectoryRepository.directoryState.value
        if (state.isFriendConnected(userId)) {
            contactDirectoryRepository.removeFriend(userId)
        } else {
            contactDirectoryRepository.addFriend(userId)
        }
    }

    fun onSecondaryActionClick() {
        val state = contactDirectoryRepository.directoryState.value
        val friend = state.findFriend(userId) ?: state.findPendingFriendRequest(userId) ?: return
        viewModelScope.launch {
            conversationRepository.findOrCreateSingleConversation(friend.who)
                .onSuccess { conversation ->
                    _openConversationId.value = conversation.conversationId
                }
                .onFailure { throwable ->
                    _errorMessage.value = "鎵撳紑浼氳瘽澶辫触"
                }
        }
    }

    fun onCommonGroupClick(groupId: Long) {
        _openGroupDetailId.value = groupId
    }

    fun onConversationNavigationConsumed() {
        _openConversationId.value = null
    }

    fun onGroupDetailNavigationConsumed() {
        _openGroupDetailId.value = null
    }

    fun onErrorMessageConsumed() {
        _errorMessage.value = null
    }
}
