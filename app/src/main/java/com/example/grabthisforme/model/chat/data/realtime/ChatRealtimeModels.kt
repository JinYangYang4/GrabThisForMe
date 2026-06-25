package com.example.grabthisforme.model.chat.data.realtime

import com.example.grabthisforme.model.message.domain.Message

sealed interface ChatRealtimeEvent {
    data object Connecting : ChatRealtimeEvent
    data object Connected : ChatRealtimeEvent
    data class ConnectionFailed(val throwable: Throwable?) : ChatRealtimeEvent
    data class MessageReceived(
        val conversationId: String,
        val message: Message
    ) : ChatRealtimeEvent
}
