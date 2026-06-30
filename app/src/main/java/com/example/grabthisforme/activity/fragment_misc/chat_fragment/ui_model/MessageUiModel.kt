package com.example.grabthisforme.activity.fragment_misc.chat_fragment.ui_model

import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.user.domain.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MessageUiModel(
    val clientMsgId: String,
    val serverMsgId: String?,
    val senderId: Long,
    val senderAvatarUrl: String?,
    val type: Message.MessageType,
    val status: Message.MessageStatus,
    val content: String?,
    val mediaUrl: String?,
    val timestamp: Long,
    val timeText: String,
    val showTime: Boolean,
    val isMine: Boolean,
    val showFailedIndicator: Boolean
)

fun List<Message>.toMessageUiModels(
    currentUser: User?,
    conversation: Conversation?,
    showTimeThresholdMillis: Long = 5 * 60 * 1000L
): List<MessageUiModel> {
    val senderMap = buildSenderMap(currentUser, conversation)
    val currentUserId = currentUser?.id
    return mapIndexed { index, message ->
        val previousTimestamp = getOrNull(index - 1)?.timestamp
        val shouldShowTime = previousTimestamp == null ||
            message.timestamp - previousTimestamp >= showTimeThresholdMillis
        MessageUiModel(
            clientMsgId = message.clientMsgId,
            serverMsgId = message.serverMsgId,
            senderId = message.senderId,
            senderAvatarUrl = senderMap[message.senderId]?.headPic,
            type = message.type,
            status = message.status,
            content = message.content,
            mediaUrl = message.mediaUrl,
            timestamp = message.timestamp,
            timeText = formatMessageTime(message.timestamp),
            showTime = shouldShowTime,
            isMine = currentUserId != null && message.senderId == currentUserId,
            showFailedIndicator = message.status == Message.MessageStatus.FAILED
        )
    }
}

private fun buildSenderMap(
    currentUser: User?,
    conversation: Conversation?
): Map<Long, User> {
    val senderMap = mutableMapOf<Long, User>()
    currentUser?.let { user ->
        senderMap[user.id] = user
    }
    when (val peer = conversation?.conversationPeer) {
        is Conversation.ConversationPeer.Single -> {
            peer.user?.let { user ->
                senderMap[user.id] = user
            }
        }

        is Conversation.ConversationPeer.Group -> {
            peer.users.forEach { user ->
                senderMap[user.id] = user
            }
        }

        null -> Unit
    }
    return senderMap
}

private fun formatMessageTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
