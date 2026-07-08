package com.example.grbt_ai_agent.charter

object AgentCharter {
    const val MISSION: String = "将 GrabThisForMe 从功能型应用推进为可感知、可规划、可执行、可协作的 Agent 化应用。"

    val PRINCIPLES: List<String> = listOf(
        "独立演进：Agent 模块独立于既有业务模块开发、测试与发布。",
        "边界清晰：Agent 通过桥接接口读取宿主能力，不直接侵入旧业务实现细节。",
        "人机协同：任何自动化能力都应允许用户理解、确认、打断和回溯。",
        "先可用再智能：先搭建任务、记忆、工具、执行框架，再逐步增强推理能力。",
        "可观测可回退：每个 Agent 行为都应可记录、可解释、可关闭。 "
    )
}
