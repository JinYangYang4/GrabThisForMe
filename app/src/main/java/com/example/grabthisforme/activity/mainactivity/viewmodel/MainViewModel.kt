package com.example.grabthisforme.activity.mainactivity.viewmodel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.chat.data.realtime.ChatRealtimeEvent
import com.example.grabthisforme.model.chat.data.realtime.ChatRealtimeManager
import com.example.grabthisforme.model.conversation.data.repository.ConversationRepository
import com.example.grabthisforme.model.friendAndGroup.data.repository.FriendAndGroupRepository
import com.example.grabthisforme.model.post.data.repository.PostRepository
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val friendAndGroupRepository: FriendAndGroupRepository,
    private val conversationRepository: ConversationRepository,
    private val postRepository: PostRepository,
    private val chatRealtimeManager: ChatRealtimeManager
) : ViewModel() {
    companion object {
        private const val TAG = "MainInitDiag"
    }

    private var _drawerOpenState = MutableLiveData(false)
    val drawerOpenState: LiveData<Boolean> get() = _drawerOpenState

    private var _openNewFragment = MutableLiveData(false)
    val openNewFragment: LiveData<Boolean> get() = _openNewFragment

    private var _selectedTab = MutableLiveData(0)
    val selectedTab: LiveData<Int> = _selectedTab

    private var _page = MutableLiveData(0)
    val page: LiveData<Int> = _page

    private val _drawerUserName = MutableLiveData("作者")
    val drawerUserName: LiveData<String> = _drawerUserName

    private val _drawerAccountText = MutableLiveData("账号：233231")
    val drawerAccountText: LiveData<String> = _drawerAccountText
    private var _currentUser = MutableLiveData<User?>()
    val currentUser : LiveData<User?> = _currentUser

    private val _isInitializing = MutableLiveData(false)
    val isInitializing: LiveData<Boolean> get() = _isInitializing

    private val _initializationError = MutableLiveData<String?>(null)
    val initializationError: LiveData<String?> get() = _initializationError

    private val _totalUnreadCount = MutableLiveData(0)
    val totalUnreadCount: LiveData<Int> get() = _totalUnreadCount

    private var hasInitializedRemoteData = false

    init {
        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _currentUser.postValue(user)
            }
        }
        viewModelScope.launch {
            conversationRepository.totalUnreadCount.collectLatest { unreadCount ->
                _totalUnreadCount.postValue(unreadCount)
            }
        }
        viewModelScope.launch {
            chatRealtimeManager.events.collectLatest { event ->
                when (event) {
                    is ChatRealtimeEvent.FriendRequestReceived -> {
                        friendAndGroupRepository.refreshRemoteFriendRequests()
                        chatRealtimeManager.ack(event.ackId)
                    }

                    is ChatRealtimeEvent.FriendRequestAccepted -> {
                        friendAndGroupRepository.refreshRemoteFriendRequests()
                        friendAndGroupRepository.refreshRemoteFriends()
                        conversationRepository.refreshRemoteConversations()
                        chatRealtimeManager.ack(event.ackId)
                    }

                    else -> Unit
                }
            }
        }
    }

    fun initializeMainDataIfNeeded(force: Boolean = false) {
        if (_isInitializing.value == true) return
        if (hasInitializedRemoteData && !force) return

        viewModelScope.launch {
            val currentUserId = userRepository.currentUserId.value
            if (currentUserId == null) {
                return@launch
            }
            _isInitializing.postValue(true)
            _initializationError.postValue(null)
            runCatching {
                friendAndGroupRepository.refreshRemoteFriends()
                friendAndGroupRepository.refreshRemoteFriendRequests()
                friendAndGroupRepository.refreshRemoteGroups()
                conversationRepository.refreshRemoteConversations()
                postRepository.refreshPosts()
                hasInitializedRemoteData = true
            }.onFailure { throwable ->
                _initializationError.postValue(throwable.message ?: "初始化失败")
            }
            _isInitializing.postValue(false)
        }
    }

    fun onInitializationErrorConsumed() {
        _initializationError.value = null
    }

    fun openNewFragment_ture() {
        _openNewFragment.value = true
    }

    fun openNewFragment_false() {
        _openNewFragment.value = false
    }

    fun drawerOpenStateToClose() {
        _drawerOpenState.value = false
    }

    fun drawerOpenStateToOpen() {
        _drawerOpenState.value = true
    }

    fun toPage(innerPage: Int) {
        _page.value = innerPage
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun updateDrawerProfile(name: String, account: String) {
        _drawerUserName.value = name
        _drawerAccountText.value = account
    }
}
