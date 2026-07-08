package com.example.grbt_ai_agent

import com.example.grbt_ai_agent.charter.AgentCharter

object GRBTAiAgentModule {
    const val MODULE_NAME: String = "GRBT_AI_Agent"
    const val MODULE_VERSION: String = "0.1.0"

    fun summary(): String {
        return "$MODULE_NAME v$MODULE_VERSION - ${AgentCharter.MISSION}"
    }
}
