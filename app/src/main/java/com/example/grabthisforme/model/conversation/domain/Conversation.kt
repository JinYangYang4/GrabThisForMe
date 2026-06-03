package com.example.grabthisforme.model.conversation.domain

import com.example.grabthisforme.model.message.domain.Message
import com.example.grabthisforme.model.user.domain.User
import java.util.UUID

data class Conversation(
    val conversationId: String,
    val type: ConversationType = ConversationType.SINGLE,
    val targetId: Long? = null,
    val conversationPeer: ConversationPeer = ConversationPeer.Single(user = null),
    val lastMessage: Message,
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
        fun generateFakeConversations(
            conversationCount: Int,
            messageCount: Int,
            currentUserId: Long = 1L
        ): List<Conversation> {
            val conversations = mutableListOf<Conversation>()

            for (index in 0 until conversationCount) {
                val user = generateFakeUser(index)
                val messages = generateFakeMessages(user, messageCount, currentUserId)

                conversations.add(
                    Conversation(
                        conversationId = UUID.randomUUID().toString(),
                        type = ConversationType.SINGLE,
                        targetId = user.id,
                        conversationPeer = ConversationPeer.Single(user),
                        lastMessage = messages.last(),
                        lastTime = System.currentTimeMillis() - (index * 1000L * 60L)
                    )
                )
            }

            return conversations
        }

        private fun generateFakeMessages(
            user: User,
            messageCount: Int,
            currentUserId: Long
        ): List<Message> {
            val messages = mutableListOf<Message>()

            for (index in 0 until messageCount) {
                val senderId = if (index % 2 == 0) currentUserId else user.id
                messages.add(
                    Message(
                        messageId = UUID.randomUUID().toString(),
                        senderId = senderId,
                        type = Message.MessageType.TEXT,
                        content = "Mock message ${index + 1} from ${user.name}",
                        timestamp = System.currentTimeMillis() - (index * 1000L * 60L),
                        status = Message.MessageStatus.READ
                    )
                )
            }

            return messages
        }

        private fun generateFakeUser(index: Int): User {
            return User(
                name = "Mock User ${index + 1}",
                id = (1000 + index).toLong(),
                headPic = "https://example.com/profile${index + 1}.jpg",
                phone = "138${(10000000..99999999).random()}",
                gender = (0..1).random(),
                isVip = Math.random() > 0.7,
                signature = "Mock signature ${index + 1}"
            )
        }
    }
}
