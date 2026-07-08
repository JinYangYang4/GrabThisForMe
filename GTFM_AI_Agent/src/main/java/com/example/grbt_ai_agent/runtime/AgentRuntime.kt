package com.example.grbt_ai_agent.runtime

import com.example.grbt_ai_agent.bridge.AgentHostBridge
import com.example.grbt_ai_agent.bridge.AgentHostEvent
import com.example.grbt_ai_agent.bridge.AgentHostEventType
import com.example.grbt_ai_agent.contract.AgentAction
import com.example.grbt_ai_agent.contract.AgentCapability
import com.example.grbt_ai_agent.contract.AgentRequest
import com.example.grbt_ai_agent.contract.AgentResponse
import com.example.grbt_ai_agent.session.AgentExecutionState
import com.example.grbt_ai_agent.session.AgentSession
import com.example.grbt_ai_agent.session.AgentSessionManager
import com.example.grbt_ai_agent.session.AgentSessionState
import kotlinx.coroutines.flow.StateFlow

class AgentRuntime(
    private val hostBridge: AgentHostBridge,
    private val sessionManager: AgentSessionManager = AgentSessionManager()
) {
    private val capabilities = linkedMapOf<String, AgentCapability>()

    val sessionState: StateFlow<AgentSessionState> = sessionManager.sessionState

    fun register(capability: AgentCapability) {
        capabilities[capability.capabilityId] = capability
    }

    fun registeredCapabilityIds(): List<String> {
        return capabilities.keys.toList()
    }

    fun activeSession(): AgentSession? {
        return sessionManager.activeSession()
    }

    fun cancelActiveSession(reason: String = "用户取消了当前任务") {
        val session = sessionManager.activeSession() ?: return
        sessionManager.cancelSession(session.sessionId, reason)
        notifyHost(AgentHostEventType.AGENT_SESSION_CANCELLED, session.sessionId, reason)
    }

    suspend fun execute(request: AgentRequest): AgentResponse {
        val session = sessionManager.startSession(request)
        notifyHost(AgentHostEventType.AGENT_SESSION_STARTED, session.sessionId, request.goal)
        return prepareOrRunSession(session, skipConfirmation = false)
    }

    suspend fun confirmAndExecute(sessionId: String): AgentResponse {
        val session = sessionManager.findSession(sessionId)
            ?: return AgentResponse(
                success = false,
                message = "未找到对应的 Agent 会话。"
            )
        if (session.executionState !is AgentExecutionState.WaitingForConfirmation) {
            return AgentResponse(
                success = false,
                message = "当前会话不处于待确认状态。"
            )
        }
        return prepareOrRunSession(session, skipConfirmation = true)
    }

    private suspend fun prepareOrRunSession(
        session: AgentSession,
        skipConfirmation: Boolean
    ): AgentResponse {
        sessionManager.markPlanning(session.sessionId, "分析用户目标并匹配可用能力")

        if (session.request.confirmationRequired && !skipConfirmation) {
            val pendingActions = listOf(
                AgentAction(type = "confirm", label = "确认执行"),
                AgentAction(type = "cancel", label = "取消任务")
            )
            sessionManager.markWaitingForConfirmation(
                sessionId = session.sessionId,
                reason = "该任务需要用户确认后才能继续执行。",
                pendingActions = pendingActions
            )
            notifyHost(
                AgentHostEventType.AGENT_SESSION_WAITING_CONFIRMATION,
                session.sessionId,
                session.request.goal
            )
            return AgentResponse(
                success = false,
                message = "任务正在等待用户确认。",
                actions = pendingActions
            )
        }

        if (session.request.networkRequired && !hostBridge.isNetworkAvailable()) {
            val response = AgentResponse(
                success = false,
                message = "当前网络不可用，无法执行需要联网的 Agent 任务。"
            )
            sessionManager.markFailed(session.sessionId, response.message, response)
            notifyHost(AgentHostEventType.AGENT_SESSION_FAILED, session.sessionId, response.message)
            return response
        }

        val capability = capabilities.values.firstOrNull { it.canHandle(session.request) }
        if (capability == null) {
            val response = AgentResponse(
                success = false,
                message = "当前没有可处理该请求的 Agent 能力。"
            )
            sessionManager.markFailed(session.sessionId, response.message, response)
            notifyHost(AgentHostEventType.AGENT_SESSION_FAILED, session.sessionId, response.message)
            return response
        }

        sessionManager.markExecuting(
            sessionId = session.sessionId,
            capabilityId = capability.capabilityId,
            detail = "调用能力 ${capability.capabilityId}"
        )
        notifyHost(
            AgentHostEventType.AGENT_SESSION_EXECUTING,
            session.sessionId,
            capability.capabilityId
        )

        return runCatching {
            capability.execute(session.request)
        }.getOrElse { throwable ->
            AgentResponse(
                success = false,
                message = throwable.message ?: "Agent 执行失败。"
            )
        }.also { response ->
            if (response.success) {
                sessionManager.markSucceeded(session.sessionId, response)
                notifyHost(AgentHostEventType.AGENT_SESSION_SUCCESS, session.sessionId, response.message)
            } else {
                sessionManager.markFailed(session.sessionId, response.message, response)
                notifyHost(AgentHostEventType.AGENT_SESSION_FAILED, session.sessionId, response.message)
            }
        }
    }

    private fun notifyHost(type: AgentHostEventType, sessionId: String, message: String) {
        hostBridge.emitHostEvent(
            AgentHostEvent(
                type = type,
                payload = mapOf(
                    "sessionId" to sessionId,
                    "message" to message
                )
            )
        )
    }
}
