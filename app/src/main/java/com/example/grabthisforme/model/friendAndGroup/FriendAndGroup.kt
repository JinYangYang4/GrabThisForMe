package com.example.grabthisforme.model.friendAndGroup

import com.example.grabthisforme.model.user.User

data class Friend(
    val friendId: Long,
    val who : User,
    val addedTime: Long = System.currentTimeMillis(),
    val status: FriendStatus = FriendStatus.PENDING
) {
    enum class FriendStatus {
        PENDING,
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
// 用于分组展示
sealed class ContactItem {
    data class FriendHeader(val title: String) : ContactItem() // 分组头部，如 “我的好友”
    data class FriendItem(val friend: Friend) : ContactItem() // 好友项
    data class GroupHeader(val title: String) : ContactItem() // 分组头部，如 “聊天群”
    data class GroupItem(val group: Group) : ContactItem() // 群聊项
}
// 新增：通用的好友/群聊选择项（无分组，可直接展示）
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