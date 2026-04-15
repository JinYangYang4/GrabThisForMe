package com.example.grabthisforme.model.conversation

import com.example.grabthisforme.model.messageContent.MessageContent
import com.example.grabthisforme.model.user.User
import java.util.*

data class Conversation(
    val conversationId: String,
    val type: ConversationType = ConversationType.SINGLE,
    val conversationPeer: ConversationPeer = ConversationPeer.Single(user = null),
    val unreadCount: Int,
    val lastMessage: MessageContent,
    val lastTime: Long
) {
    enum class ConversationType {
        SINGLE,
        GROUP
    }

    sealed class ConversationPeer {
        data class Single(val user: User?) : ConversationPeer()
        data class Group(val users: List<User>) : ConversationPeer()
    }

    companion object {
        // 生成虚拟对话数据的内部方法
        fun generateFakeConversations(
            conversationCount: Int,
            messageCount: Int
        ): List<Conversation> {
            val conversations = mutableListOf<Conversation>()

            // 生成虚拟对话
            for (i in 0 until conversationCount) {
                val user = generateFakeUser(i)

                // 生成虚拟消息
                val messages = generateFakeMessages(user, messageCount)

                val conversation = Conversation(
                    conversationId = UUID.randomUUID().toString(),
                    type = ConversationType.SINGLE,
                    conversationPeer = ConversationPeer.Single(user),
                    unreadCount = (0..10).random(),
                    lastMessage = messages.last(),
                    lastTime = System.currentTimeMillis() - (i * 1000L * 60L)
                )

                conversations.add(conversation)
            }

            return conversations
        }

        // 生成虚拟消息内容的内部方法
        private fun generateFakeMessages(user: User, messageCount: Int): List<MessageContent> {
            val messages = mutableListOf<MessageContent>()

            // 生成虚拟消息
            for (i in 0 until messageCount) {
                val message = MessageContent(
                    messageId = UUID.randomUUID().toString(),
                    type = MessageContent.MessageType.TEXT,
                    content = "虚拟消息 ${i + 1} 来自 ${user.name}",
                    timestamp = System.currentTimeMillis() - (i * 1000L * 60L),
                    isMine = (i % 2 == 0), // 随机消息是否为自己发送
                    status = MessageContent.MessageStatus.READ
                )
                messages.add(message)
            }

            return messages
        }

        // 生成虚拟用户的方法
        private fun generateFakeUser(index: Int): User {
            return User(
                name = "虚拟用户 ${index + 1}",
                id = (1000 + index).toLong(),
                headPic = "https://example.com/profile${index + 1}.jpg",
                phone = "138${(10000000..99999999).random()}",
                gender = (0..1).random(),
                isVip = (Math.random() > 0.7),  // 随机生成VIP状态
                signature = "这是虚拟用户 ${index + 1} 的签名"
            )
        }
    }
}
