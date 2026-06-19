package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui.state

data class CommentUiState(
    val commentId: Long,
    var isExpanded: Boolean = false,
    var visibleReplyCount: Int = 0,
    var hasMoreReply: Boolean = false,
    var expandStage: Int = 0
) {
    fun expand(totalReplyCount: Int, targetCount: Int) {
        isExpanded = true
        visibleReplyCount = totalReplyCount.coerceAtMost(targetCount)
        hasMoreReply = visibleReplyCount < totalReplyCount
        if (expandStage == 0) {
            expandStage = 1
        }
    }

    fun expandAddReply() {
        isExpanded = true
        visibleReplyCount += 1
    }

    fun loadMore(totalReplyCount: Int, increment: Int) {
        isExpanded = true
        visibleReplyCount = (visibleReplyCount + increment).coerceAtMost(totalReplyCount)
        hasMoreReply = visibleReplyCount < totalReplyCount
        expandStage += 1
    }

    fun nextLoadTargetCount(): Int {
        return when (expandStage) {
            0 -> visibleReplyCount + 3
            1 -> visibleReplyCount + 5
            else -> visibleReplyCount + 7
        }
    }

    fun collapse() {
        isExpanded = false
        visibleReplyCount = 0
        hasMoreReply = false
        expandStage = 0
    }
}
