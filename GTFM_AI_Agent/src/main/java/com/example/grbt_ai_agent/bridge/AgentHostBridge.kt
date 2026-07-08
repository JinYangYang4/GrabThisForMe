package com.example.grbt_ai_agent.bridge

interface AgentHostBridge {
    fun currentUserId(): Long?
    fun currentSessionToken(): String?
    fun isNetworkAvailable(): Boolean
    fun emitHostEvent(event: AgentHostEvent)
}

enum class AgentHostEventType {
    AGENT_SESSION_STARTED,
    AGENT_SESSION_EXECUTING,
    AGENT_SESSION_WAITING_CONFIRMATION,
    AGENT_SESSION_SUCCESS,
    AGENT_SESSION_FAILED,
    AGENT_SESSION_CANCELLED
}

data class AgentHostEvent(
    val type: AgentHostEventType,
    val payload: Map<String, String> = emptyMap()
)
