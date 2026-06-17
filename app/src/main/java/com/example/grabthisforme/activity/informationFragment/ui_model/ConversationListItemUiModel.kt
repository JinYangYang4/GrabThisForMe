package com.example.grabthisforme.activity.informationFragment.ui_model

import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.message.domain.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversationListItemUiModel(
    val conversationId: String,
    val title: String,
    val avatarUrl: String?,
    val lastMessageText: String,
    val timeText: String,
    val unreadCount: Int,
    val showUnreadBadge: Boolean
)

fun Conversation.toConversationListItemUiModel(
    state: ConversationUserStateEntity?
): ConversationListItemUiModel {
    val title = when (val peer = conversationPeer) {
        is Conversation.ConversationPeer.Single -> peer.user?.name.orEmpty().ifBlank { "未命名用户" }
        is Conversation.ConversationPeer.Group -> "群聊"
    }
    val avatarUrl = when (val peer = conversationPeer) {
        is Conversation.ConversationPeer.Single -> peer.user?.headPic
        is Conversation.ConversationPeer.Group -> null
    }
    val unreadCount = state?.unreadCount ?: 0
    return ConversationListItemUiModel(
        conversationId = conversationId,
        title = title,
        avatarUrl = avatarUrl,
        lastMessageText = buildLastMessageText(lastMessage),
        timeText = formatConversationTime(lastTime),
        unreadCount = unreadCount,
        showUnreadBadge = unreadCount > 0
    )
}

private fun buildLastMessageText(message: Message): String {
    return when (message.type) {
        Message.MessageType.IMAGE -> "[图片]"
        Message.MessageType.VOICE -> "[语音]"
        Message.MessageType.SYSTEM -> message.content.orEmpty().ifBlank { "[系统消息]" }
        Message.MessageType.TEXT -> message.content.orEmpty()
    }
}

private fun formatConversationTime(timestamp: Long): String {
    val timeInMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timeInMillis))
}
