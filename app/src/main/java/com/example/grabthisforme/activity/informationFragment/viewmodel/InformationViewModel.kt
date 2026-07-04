package com.example.grabthisforme.activity.informationFragment.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
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
                val pendingRequests = state.pendingFriendRequests.filter { request ->
                    request.status == Friend.FriendStatus.PENDING_RECEIVED
                }
                val connectedFriends = state.friends.filter { friend ->
                    state.isFriendConnected(friend.friendId)
                }
                val joinedGroups = state.groups.filter { group ->
                    state.isGroupJoined(group.groupId)
                }

                add(ContactItem.FriendHeader("联系人"))
                connectedFriends.forEach { friend ->
                    add(ContactItem.FriendItem(friend))
                }

                add(ContactItem.GroupHeader("鑱婄兢"))
                joinedGroups.forEach { group ->
                    add(ContactItem.GroupItem(group))
                }
            }
        }
        .asLiveData()

    val newFriendUnreadCount: LiveData<Int> = contactDirectoryRepository.directoryState
        .map { state ->
            state.pendingFriendRequests.count { request ->
                request.status == Friend.FriendStatus.PENDING_RECEIVED
            }
        }
        .asLiveData()

    val newFriendSubtitle: LiveData<String> = contactDirectoryRepository.directoryState
        .map { state ->
            val count = state.pendingFriendRequests.count { request ->
                request.status == Friend.FriendStatus.PENDING_RECEIVED
            }
            if (count == 0) {
                "暂时没有新的好友申请"
            } else {
                "你有 $count 条新的好友申请"
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
                    _errorMessage.value = "鎵撳紑浼氳瘽澶辫触"
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
                _errorMessage.value = "鎵撳紑缇よ亰澶辫触"
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

    suspend fun refreshRemoteConversations() {
        conversationRepository.refreshRemoteConversations()
    }

    fun onErrorMessageConsumed() {
        _errorMessage.value = null
    }
}
