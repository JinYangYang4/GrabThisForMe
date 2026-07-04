package com.example.grabthisforme.model.friendAndGroup

import com.example.grabthisforme.model.user.domain.User

data class Friend(
    val friendId: Long,
    val who: User,
    val addedTime: Long = System.currentTimeMillis(),
    val status: FriendStatus = FriendStatus.PENDING_SENT
) {
    enum class FriendStatus {
        PENDING_SENT,
        PENDING_RECEIVED,
        ACCEPTED,
        REJECTED
    }
}

data class Group(
    val groupId: Long,
    val groupName: String,
    val members: List<User>,
    val createTime: Long = System.currentTimeMillis()
)

sealed class ContactItem {
    data class FriendHeader(val title: String) : ContactItem()
    data class FriendItem(val friend: Friend) : ContactItem()
    data class GroupHeader(val title: String) : ContactItem()
    data class GroupItem(val group: Group) : ContactItem()
}

sealed class SelectableItem : ContactItem() {
    data class SelectableFriend(
        val friend: Friend,
        val isSelected: Boolean = false
    ) : SelectableItem()

    data class SelectableGroup(
        val group: Group,
        val isSelected: Boolean = false
    ) : SelectableItem()
}
