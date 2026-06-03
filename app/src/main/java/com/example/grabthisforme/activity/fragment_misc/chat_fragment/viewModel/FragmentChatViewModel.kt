package com.example.grabthisforme.activity.fragment_misc.chat_fragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.ui_model.ChatConversationUiModel
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.ui_model.MessageUiModel
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.ui_model.toMessageUiModels
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryRepository
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryState
import com.example.grabthisforme.model.message.data.repository.MessageRepository
import com.example.grabthisforme.model.user.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class FragmentChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val conversationRepository: ConversationRepository,
    private val contactDirectoryRepository: ContactDirectoryRepository
) : ViewModel() {

    private val _keyboardState: MutableLiveData<Boolean> = MutableLiveData(false)
    val keyboardStatus: LiveData<Boolean> get() = _keyboardState

    private val _canSend = MutableLiveData(false)
    val canSend: LiveData<Boolean> get() = _canSend

    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> get() = _inputText

    private val _openUserDetailId = MutableLiveData<Long?>(null)
    val openUserDetailId: LiveData<Long?> get() = _openUserDetailId

    private val _openGroupDetailId = MutableLiveData<Long?>(null)
    val openGroupDetailId: LiveData<Long?> get() = _openGroupDetailId

    private val _conversationId = MutableStateFlow<String?>(null)

    private val currentConversation = combine(
        _conversationId,
        conversationRepository.allConversations
    ) { conversationId, conversations ->
        conversations.firstOrNull { it.conversationId == conversationId }
    }

    val conversationUiModel: LiveData<ChatConversationUiModel?> = combine(
        currentConversation,
        contactDirectoryRepository.directoryState
    ) { conversation, directoryState ->
        conversation?.toChatConversationUiModel(directoryState)
        }
        .asLiveData()

    val messages: LiveData<List<MessageUiModel>> = _conversationId
        .flatMapLatest { conversationId ->
            if (conversationId == null) {
                flowOf(emptyList())
            } else {
                combine(
                    messageRepository.getMessagesByConversation(conversationId),
                    userRepository.currentUser,
                    currentConversation
                ) { messages, currentUser, conversation ->
                    messages.toMessageUiModels(currentUser, conversation)
                }
            }
        }
        .asLiveData()

    fun turnKeyboardStateToTure() {
        if (_keyboardState.value != true) {
            _keyboardState.value = true
        }
    }

    fun turnKeyboardStateToFalse() {
        if (_keyboardState.value != false) {
            _keyboardState.value = false
        }
    }

    fun onInputChanged(input: String) {
        _inputText.value = input
        _canSend.value = input.trim().isNotEmpty()
    }

    fun clearInputState() {
        _inputText.value = ""
        _canSend.value = false
    }

    fun loadMessages(conversationId: String) {
        _conversationId.value = conversationId
        viewModelScope.launch {
            conversationRepository.markConversationAsRead(conversationId)
        }
    }

    fun onTopAvatarClick() {
        val conversation = currentConversationValue() ?: return
        when (conversation.type) {
            Conversation.ConversationType.SINGLE -> {
                val userId = (conversation.conversationPeer as? Conversation.ConversationPeer.Single)
                    ?.user
                    ?.id
                    ?: return
                _openUserDetailId.value = userId
            }

            Conversation.ConversationType.GROUP -> {
                val groupId = conversation.targetId ?: return
                _openGroupDetailId.value = groupId
            }
        }
    }

    fun onPeerAvatarClick(senderId: Long) {
        val currentUserId = userRepository.currentUserId.value
        if (senderId <= 0L || senderId == currentUserId) return
        _openUserDetailId.value = senderId
    }

    fun onUserDetailNavigationConsumed() {
        _openUserDetailId.value = null
    }

    fun onGroupDetailNavigationConsumed() {
        _openGroupDetailId.value = null
    }

    fun sendTextMessage(conversationId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            messageRepository.sendTextMessage(conversationId, text)
        }
    }

    fun sendImageMessage(conversationId: String, mediaUrl: String) {
        if (mediaUrl.isBlank()) return
        viewModelScope.launch {
            messageRepository.sendImageMessage(conversationId, mediaUrl)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            messageRepository.deleteMessage(messageId)
        }
    }

    private fun currentConversationValue(): Conversation? {
        val conversationId = _conversationId.value ?: return null
        return conversationRepository.allConversations.value.firstOrNull { conversation ->
            conversation.conversationId == conversationId
        }
    }

    private fun Conversation.toChatConversationUiModel(
        directoryState: ContactDirectoryState
    ): ChatConversationUiModel {
        return when (val peer = conversationPeer) {
            is Conversation.ConversationPeer.Single -> {
                val user = peer.user
                ChatConversationUiModel(
                    title = user?.name?.takeIf { it.isNotBlank() } ?: "聊天",
                    subtitle = "点击头像查看详细资料",
                    avatarUrl = user?.headPic,
                    isGroup = false,
                    userDetailId = user?.id
                )
            }

            is Conversation.ConversationPeer.Group -> {
                val group = targetId?.let(directoryState::findGroup)
                ChatConversationUiModel(
                    title = group?.groupName?.takeIf { it.isNotBlank() } ?: "群聊",
                    subtitle = "点击头像查看聊群信息",
                    avatarUrl = null,
                    isGroup = true,
                    groupDetailId = targetId
                )
            }
        }
    }
}
