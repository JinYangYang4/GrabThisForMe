package com.example.grbt_ai_agent.session

import com.example.grbt_ai_agent.contract.AgentAction
import com.example.grbt_ai_agent.contract.AgentRequest
import com.example.grbt_ai_agent.contract.AgentResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgentSessionManager {
    private val _sessionState = MutableStateFlow(AgentSessionState())
    val sessionState: StateFlow<AgentSessionState> = _sessionState.asStateFlow()

    fun startSession(request: AgentRequest): AgentSession {
        archiveActiveSessionIfNecessary(replacementReason = "被新的任务替换")
        val now = System.currentTimeMillis()
        val session = AgentSession(
            request = request,
            createdAt = now,
            updatedAt = now,
            startedAt = now,
            traces = listOf(
                AgentTrace(
                    timestamp = now,
                    label = "SESSION_CREATED",
                    detail = "创建新的 Agent 会话"
                )
            )
        )
        _sessionState.value = _sessionState.value.copy(activeSession = session)
        return session
    }

    fun activeSession(): AgentSession? {
        return _sessionState.value.activeSession
    }

    fun findSession(sessionId: String): AgentSession? {
        val state = _sessionState.value
        return when {
            state.activeSession?.sessionId == sessionId -> state.activeSession
            else -> state.recentSessions.firstOrNull { it.sessionId == sessionId }
        }
    }

    fun markPlanning(sessionId: String, summary: String) {
        updateActiveSession(sessionId) { session, now ->
            session.copy(
                executionState = AgentExecutionState.Planning(summary),
                updatedAt = now,
                traces = session.traces + AgentTrace(now, "PLANNING", summary)
            )
        }
    }

    fun markExecuting(sessionId: String, capabilityId: String, detail: String) {
        updateActiveSession(sessionId) { session, now ->
            session.copy(
                executionState = AgentExecutionState.Executing(capabilityId, detail),
                updatedAt = now,
                selectedCapabilityId = capabilityId,
                traces = session.traces + AgentTrace(now, "EXECUTING", detail)
            )
        }
    }

    fun markWaitingForConfirmation(sessionId: String, reason: String, pendingActions: List<AgentAction>) {
        updateActiveSession(sessionId) { session, now ->
            session.copy(
                executionState = AgentExecutionState.WaitingForConfirmation(reason, pendingActions),
                updatedAt = now,
                traces = session.traces + AgentTrace(now, "WAITING_CONFIRMATION", reason)
            )
        }
    }

    fun markSucceeded(sessionId: String, response: AgentResponse) {
        updateActiveSession(sessionId) { session, now ->
            session.copy(
                executionState = AgentExecutionState.Success(response.message),
                updatedAt = now,
                completedAt = now,
                latestResponse = response,
                lastErrorMessage = null,
                traces = session.traces + AgentTrace(now, "SUCCESS", response.message)
            )
        }
    }

    fun markFailed(sessionId: String, reason: String, response: AgentResponse? = null) {
        updateActiveSession(sessionId) { session, now ->
            session.copy(
                executionState = AgentExecutionState.Failed(reason),
                updatedAt = now,
                completedAt = now,
                latestResponse = response,
                lastErrorMessage = reason,
                traces = session.traces + AgentTrace(now, "FAILED", reason)
            )
        }
    }

    fun cancelSession(sessionId: String, reason: String) {
        updateActiveSession(sessionId) { session, now ->
            session.copy(
                executionState = AgentExecutionState.Cancelled(reason),
                updatedAt = now,
                completedAt = now,
                lastErrorMessage = reason,
                traces = session.traces + AgentTrace(now, "CANCELLED", reason)
            )
        }
    }

    fun clearActiveSession() {
        archiveActiveSessionIfNecessary(replacementReason = null)
        _sessionState.value = _sessionState.value.copy(activeSession = null)
    }

    private fun archiveActiveSessionIfNecessary(replacementReason: String?) {
        val currentState = _sessionState.value
        val activeSession = currentState.activeSession ?: return
        val archivedSession = if (
            replacementReason != null &&
            activeSession.executionState !is AgentExecutionState.Success &&
            activeSession.executionState !is AgentExecutionState.Failed &&
            activeSession.executionState !is AgentExecutionState.Cancelled
        ) {
            val now = System.currentTimeMillis()
            activeSession.copy(
                executionState = AgentExecutionState.Cancelled(replacementReason),
                updatedAt = now,
                completedAt = now,
                lastErrorMessage = replacementReason,
                traces = activeSession.traces + AgentTrace(now, "CANCELLED", replacementReason)
            )
        } else {
            activeSession
        }
        _sessionState.value = currentState.copy(
            activeSession = null,
            recentSessions = listOf(archivedSession) + currentState.recentSessions
        )
    }

    private fun updateActiveSession(
        sessionId: String,
        block: (AgentSession, Long) -> AgentSession
    ) {
        val currentState = _sessionState.value
        val activeSession = currentState.activeSession ?: return
        if (activeSession.sessionId != sessionId) return
        val now = System.currentTimeMillis()
        val updatedSession = block(activeSession, now)
        _sessionState.value = currentState.copy(activeSession = updatedSession)
    }
}
