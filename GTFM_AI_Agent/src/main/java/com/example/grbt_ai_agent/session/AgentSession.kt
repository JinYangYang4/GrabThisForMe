package com.example.grbt_ai_agent.session

import com.example.grbt_ai_agent.contract.AgentAction
import com.example.grbt_ai_agent.contract.AgentRequest
import com.example.grbt_ai_agent.contract.AgentResponse
import java.util.UUID

data class AgentSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val request: AgentRequest,
    val executionState: AgentExecutionState = AgentExecutionState.Idle,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val selectedCapabilityId: String? = null,
    val latestResponse: AgentResponse? = null,
    val lastErrorMessage: String? = null,
    val traces: List<AgentTrace> = emptyList()
)

sealed interface AgentExecutionState {
    data object Idle : AgentExecutionState
    data class Planning(val summary: String) : AgentExecutionState
    data class Executing(val capabilityId: String, val detail: String) : AgentExecutionState
    data class WaitingForConfirmation(
        val reason: String,
        val pendingActions: List<AgentAction> = emptyList()
    ) : AgentExecutionState
    data class Success(val summary: String) : AgentExecutionState
    data class Failed(val reason: String) : AgentExecutionState
    data class Cancelled(val reason: String) : AgentExecutionState
}

data class AgentTrace(
    val timestamp: Long = System.currentTimeMillis(),
    val label: String,
    val detail: String
)

data class AgentSessionState(
    val activeSession: AgentSession? = null,
    val recentSessions: List<AgentSession> = emptyList()
) {
    val executionState: AgentExecutionState
        get() = activeSession?.executionState ?: AgentExecutionState.Idle
}
