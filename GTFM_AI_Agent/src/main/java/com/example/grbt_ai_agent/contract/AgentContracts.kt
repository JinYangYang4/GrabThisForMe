package com.example.grbt_ai_agent.contract

data class AgentRequest(
    val goal: String,
    val context: Map<String, String> = emptyMap(),
    val confirmationRequired: Boolean = false,
    val networkRequired: Boolean = false
)

data class AgentResponse(
    val success: Boolean,
    val message: String,
    val actions: List<AgentAction> = emptyList()
)

data class AgentAction(
    val type: String,
    val label: String,
    val payload: Map<String, String> = emptyMap()
)

interface AgentCapability {
    val capabilityId: String
    fun canHandle(request: AgentRequest): Boolean
    suspend fun execute(request: AgentRequest): AgentResponse
}
