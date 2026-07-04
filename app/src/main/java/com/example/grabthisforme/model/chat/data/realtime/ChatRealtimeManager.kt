package com.example.grabthisforme.model.chat.data.realtime
import com.example.grabthisforme.di.NetworkModule
import com.example.grabthisforme.model.conversation.data.network.dto.ConversationSocketPayloadDto
import com.example.grabthisforme.model.message.mapper.toDomain
import com.example.grabthisforme.model.network.AuthTokenDataStore
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

@Singleton
class ChatRealtimeManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val authTokenDataStore: AuthTokenDataStore,
    private val gson: Gson
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _events = MutableSharedFlow<ChatRealtimeEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ChatRealtimeEvent> = _events

    @Volatile
    private var webSocket: WebSocket? = null

    @Volatile
    private var isConnecting = false

    @Volatile
    private var isConnected = false

    fun connectIfNeeded() {
        if (webSocket != null || isConnecting) return
        scope.launch {
            val token = authTokenDataStore.getToken().orEmpty()
            if (token.isBlank()) return@launch

            isConnecting = true
            _events.tryEmit(ChatRealtimeEvent.Connecting)

            val wsUrl = NetworkModule.BASE_URL
                .replaceFirst("http://", "ws://")
                .replaceFirst("https://", "wss://")
                .trimEnd('/') + "/ws?token=$token"

            val request = Request.Builder()
                .url(wsUrl)
                .addHeader("Authorization", "Bearer $token")
                .build()
            webSocket = okHttpClient.newWebSocket(request, listener)
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "disconnect")
        webSocket = null
        isConnecting = false
        isConnected = false
    }

    fun ack(ackId: String?) {
        if (ackId.isNullOrBlank()) return
        sendStompFrame(
            command = "ACK",
            headers = mapOf("id" to ackId)
        )
    }

    private fun sendStompFrame(
        command: String,
        headers: Map<String, String> = emptyMap(),
        body: String = ""
    ) {
        val socket = webSocket ?: return
        val frame = buildString {
            append(command).append('\n')
            headers.forEach { (key, value) ->
                append(key).append(':').append(value).append('\n')
            }
            append('\n')
            append(body)
            append('\u0000')
        }
        socket.send(frame)
    }

    private fun handleStompMessage(frameText: String) {
        frameText
            .split('\u0000')
            .map { it.trim('\u0000', '\r', '\n') }
            .filter { it.isNotBlank() }
            .forEach { rawFrame ->
                val headerEndIndex = rawFrame.indexOf("\n\n")
                val headerPart = if (headerEndIndex >= 0) rawFrame.substring(0, headerEndIndex) else rawFrame
                val body = if (headerEndIndex >= 0) rawFrame.substring(headerEndIndex + 2) else ""
                val headerLines = headerPart.lineSequence().toList()
                val command = headerLines.firstOrNull().orEmpty()
                val headers = headerLines
                    .drop(1)
                    .mapNotNull { line ->
                        val separatorIndex = line.indexOf(':')
                        if (separatorIndex <= 0) {
                            null
                        } else {
                            line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
                        }
                    }
                    .toMap()

                when (command) {
                    "CONNECTED" -> {
                        isConnecting = false
                        isConnected = true
                        sendStompFrame(
                            command = "SUBSCRIBE",
                            headers = mapOf(
                                "id" to "sub-user-queue-messages",
                                "destination" to "/user/queue/messages",
                                "ack" to "client-individual"
                            )
                        )
                        _events.tryEmit(ChatRealtimeEvent.Connected)
                    }

                    "MESSAGE" -> {
                        val ackId = headers["ack"] ?: headers["message-id"]
                        runCatching {
                            val payload = gson.fromJson(body, ConversationSocketPayloadDto::class.java)
                            when (payload.type) {
                                "conversation.message" -> {
                                    val message = payload.message?.toDomain() ?: return@runCatching
                                    val conversationId = payload.conversationId ?: return@runCatching
                                    _events.tryEmit(
                                        ChatRealtimeEvent.MessageReceived(
                                            conversationId = conversationId,
                                            message = message,
                                            ackId = ackId
                                        )
                                    )
                                }

                                "friend.request.received" -> {
                                    val friendUserId = payload.friendRequest?.userId ?: return@runCatching
                                    _events.tryEmit(
                                        ChatRealtimeEvent.FriendRequestReceived(
                                            friendUserId = friendUserId,
                                            ackId = ackId
                                        )
                                    )
                                }

                                "friend.request.accepted" -> {
                                    val friendUserId = payload.friendRequest?.userId ?: return@runCatching
                                    _events.tryEmit(
                                        ChatRealtimeEvent.FriendRequestAccepted(
                                            friendUserId = friendUserId,
                                            ackId = ackId
                                        )
                                    )
                                }
                            }
                        }.onFailure { throwable ->
                        }
                    }

                    "ERROR" -> {
                    }
                }
            }
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            this@ChatRealtimeManager.webSocket = webSocket
            sendStompFrame(
                command = "CONNECT",
                headers = mapOf(
                    "accept-version" to "1.2",
                    "heart-beat" to "0,0"
                )
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            handleStompMessage(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            this@ChatRealtimeManager.webSocket = null
            isConnecting = false
            isConnected = false
            _events.tryEmit(ChatRealtimeEvent.ConnectionFailed(t))
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            this@ChatRealtimeManager.webSocket = null
            isConnecting = false
            isConnected = false
        }
    }
}

