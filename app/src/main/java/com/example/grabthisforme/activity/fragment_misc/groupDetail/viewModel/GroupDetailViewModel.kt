package com.example.grabthisforme.activity.fragment_misc.groupDetail.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.groupDetail.ui_model.GroupDetailUiModel
import com.example.grabthisforme.activity.fragment_misc.groupDetail.ui_model.GroupMemberItemUiModel
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contactDirectoryRepository: ContactDirectoryRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle["groupId"] ?: -1L

    private val _uiModel = MutableLiveData<GroupDetailUiModel>()
    val uiModel: LiveData<GroupDetailUiModel> get() = _uiModel

    private val _memberList = MutableLiveData<List<GroupMemberItemUiModel>>(emptyList())
    val memberList: LiveData<List<GroupMemberItemUiModel>> get() = _memberList

    private val _openConversationId = MutableLiveData<String?>(null)
    val openConversationId: LiveData<String?> get() = _openConversationId

    private val _openUserDetailId = MutableLiveData<Long?>(null)
    val openUserDetailId: LiveData<Long?> get() = _openUserDetailId

    init {
        viewModelScope.launch {
            contactDirectoryRepository.directoryState.collectLatest { state ->
                val group = state.findGroup(groupId) ?: return@collectLatest
                val joined = state.isGroupJoined(groupId)
                _uiModel.value = GroupDetailUiModel(
                    groupId = group.groupId,
                    groupName = group.groupName,
                    memberCountText = "${group.members.size} 位成员",
                    statusText = if (joined) "你已加入这个群聊，可直接查看讨论" else "还未加入，可先加入再参与互助",
                    primaryActionText = if (joined) "退出群聊" else "加入群聊",
                    secondaryActionText = if (joined) "进入群聊" else "临时会话",
                    isJoined = joined,
                    sceneText = "适合跑腿、拼单、求助、闲置交换等校园即时场景",
                    managerText = "群主 ${group.members.firstOrNull()?.name ?: "校园助手"}",
                    vibeText = "成员大多活跃在生活区、教学楼和快递站附近",
                    tipsText = "建议先看群公告和近期消息，再发起求助或拼单，会更容易获得响应"
                )
                _memberList.value = group.members.mapIndexed { index, user ->
                    GroupMemberItemUiModel(
                        userId = user.id,
                        name = user.name,
                        subtitle = user.signature ?: "校园互助中",
                        isManager = index == 0
                    )
                }
            }
        }
    }

    fun onPrimaryActionClick() {
        val state = contactDirectoryRepository.directoryState.value
        if (state.isGroupJoined(groupId)) {
            contactDirectoryRepository.leaveGroup(groupId)
        } else {
            contactDirectoryRepository.joinGroup(groupId)
        }
    }

    fun onSecondaryActionClick() {
        val state = contactDirectoryRepository.directoryState.value
        val group = state.findGroup(groupId) ?: return
        viewModelScope.launch {
            val conversation = conversationRepository.findOrCreateGroupConversation(
                groupId = group.groupId,
                members = group.members
            )
            _openConversationId.value = conversation.conversationId
        }
    }

    fun onMemberClick(userId: Long) {
        _openUserDetailId.value = userId
    }

    fun onConversationNavigationConsumed() {
        _openConversationId.value = null
    }

    fun onUserDetailNavigationConsumed() {
        _openUserDetailId.value = null
    }
}
