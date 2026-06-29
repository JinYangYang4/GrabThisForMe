package com.example.grabthisforme.activity.informationFragment.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.informationFragment.ui_model.ConversationListItemUiModel
import com.example.grabthisforme.activity.informationFragment.ui_model.toConversationListItemUiModel
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.friendAndGroup.ContactItem
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class InformationViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val contactDirectoryRepository: ContactDirectoryRepository
) : ViewModel() {

    val conversations: LiveData<List<ConversationListItemUiModel>> = combine(
        conversationRepository.allConversations,
        conversationRepository.currentUserConversationStates
    ) { conversations, states ->
        val statesByConversationId = states.associateBy { it.conversationId }
        conversations.map { conversation ->
            conversation.toConversationListItemUiModel(statesByConversationId[conversation.conversationId])
        }
    }.asLiveData()

    val contactItems: LiveData<List<ContactItem>> = contactDirectoryRepository.directoryState
        .map { state ->
            buildList {
                val connectedFriends = state.friends.filter { friend ->
                    state.isFriendConnected(friend.friendId)
                }
                val joinedGroups = state.groups.filter { group ->
                    state.isGroupJoined(group.groupId)
                }

                if (connectedFriends.isNotEmpty()) {
                    add(ContactItem.FriendHeader("联系人"))
                    connectedFriends.forEach { friend ->
                        add(ContactItem.FriendItem(friend))
                    }
                }

                if (joinedGroups.isNotEmpty()) {
                    add(ContactItem.GroupHeader("聊群"))
                    joinedGroups.forEach { group ->
                        add(ContactItem.GroupItem(group))
                    }

                }
            }
        }
        .asLiveData()

    private val _openConversationId = MutableLiveData<String?>(null)
    val openConversationId: LiveData<String?> = _openConversationId

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    fun onFriendClicked(friend: Friend) {
        viewModelScope.launch {
            conversationRepository.findOrCreateSingleConversation(friend.who)
                .onSuccess { conversation ->
                    _openConversationId.value = conversation.conversationId
                }
                .onFailure { throwable ->
                    Log.e("InformationViewModel", "open single conversation failed", throwable)
                    _errorMessage.value = "打开会话失败"
                }
        }
    }

    fun onGroupClicked(group: Group) {
        viewModelScope.launch {
            conversationRepository.findOrCreateGroupConversation(
                groupId = group.groupId,
                members = group.members
            ).onSuccess { conversation ->
                _openConversationId.value = conversation.conversationId
            }.onFailure { throwable ->
                Log.e("InformationViewModel", "open group conversation failed", throwable)
                _errorMessage.value = "打开群聊失败"
            }
        }
    }

    fun onConversationNavigationConsumed() {
        _openConversationId.value = null
    }

    fun hideConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.setConversationHidden(conversationId, true)
        }
    }

    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversationById(conversationId)
            val lastSeenTime = conversation?.lastMessage?.timestamp
            conversationRepository.markConversationAsRead(conversationId, lastSeenTime)
        }
    }
    suspend fun refreshRemoteConversations(){
        conversationRepository.refreshRemoteConversations()
    }

    fun onErrorMessageConsumed() {
        _errorMessage.value = null
    }
}
