package com.example.grabthisforme.activity.fragment_misc.new_friend.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.search.friend.ui_model.SearchContactResultUiModel
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.data.repository.ContactDirectoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class NewFriendViewModel @Inject constructor(
    private val contactDirectoryRepository: ContactDirectoryRepository
) : ViewModel() {

    val requestItems: LiveData<List<SearchContactResultUiModel>> = contactDirectoryRepository.directoryState
        .map { state ->
            val incoming = state.pendingFriendRequests
                .filter { it.status == Friend.FriendStatus.PENDING_RECEIVED }
                .map { friend ->
                    friend.toUiModel(
                        statusText = "对方想添加你为好友",
                        actionText = "同意",
                        actionEnabled = true,
                        connectedText = null,
                        sortPriority = 0
                    )
                }

            val outgoing = state.pendingFriendRequests
                .filter { it.status == Friend.FriendStatus.PENDING_SENT }
                .map { friend ->
                    friend.toUiModel(
                        statusText = "你已发送好友申请，等待对方同意",
                        actionText = null,
                        actionEnabled = false,
                        connectedText = "待同意",
                        sortPriority = 1
                    )
                }

            val acceptedFromPending = state.pendingFriendRequests
                .filter { it.status == Friend.FriendStatus.ACCEPTED }
                .map { friend ->
                    friend.toUiModel(
                        statusText = "你们已经成为好友",
                        actionText = null,
                        actionEnabled = false,
                        connectedText = "已同意",
                        sortPriority = 2
                    )
                }

            val accepted = state.friends
                .filter { friend -> state.isFriendConnected(friend.friendId) }
                .map { friend ->
                    friend.toUiModel(
                        statusText = "你们已经成为好友",
                        actionText = null,
                        actionEnabled = false,
                        connectedText = "已成为好友",
                        sortPriority = 3
                    )
                }

            (incoming + outgoing + acceptedFromPending + accepted)
                .distinctBy { it.stableId }
                .sortedBy { item ->
                    when (item.connectedText ?: item.actionText) {
                        "同意" -> 0
                        "待同意" -> 1
                        "已同意" -> 2
                        else -> 3
                    }
                }
        }
        .asLiveData()

    private val _openUserDetailId = MutableLiveData<Long?>(null)
    val openUserDetailId: LiveData<Long?> = _openUserDetailId

    fun onItemClick(stableId: String) {
        parseFriendId(stableId)?.let { friendId ->
            _openUserDetailId.value = friendId
        }
    }

    fun onActionClick(stableId: String) {
        val friendId = parseFriendId(stableId) ?: return
        viewModelScope.launch {
            contactDirectoryRepository.acceptFriendRequest(friendId)
        }
    }

    fun onUserDetailNavigationConsumed() {
        _openUserDetailId.value = null
    }

    private fun parseFriendId(stableId: String): Long? {
        return stableId.removePrefix("friend_").toLongOrNull()
    }

    private fun Friend.toUiModel(
        statusText: String,
        actionText: String?,
        actionEnabled: Boolean,
        connectedText: String?,
        sortPriority: Int
    ): SearchContactResultUiModel {
        return SearchContactResultUiModel(
            stableId = "friend_$friendId",
            title = who.name,
            subtitle = buildSubtitle(this, sortPriority),
            badgeText = "联系人",
            statusText = statusText,
            actionText = actionText,
            actionEnabled = actionEnabled,
            isFriend = true,
            isConnected = false,
            connectedText = connectedText
        )
    }

    private fun buildSubtitle(friend: Friend, sortPriority: Int): String {
        val signature = friend.who.signature?.takeIf { it.isNotBlank() } ?: "校园互助用户"
        val prefix = when (sortPriority) {
            0 -> "收到申请"
            1 -> "我发出的申请"
            else -> "好友关系"
        }
        return "$prefix · ID ${friend.friendId} · $signature"
    }
}
