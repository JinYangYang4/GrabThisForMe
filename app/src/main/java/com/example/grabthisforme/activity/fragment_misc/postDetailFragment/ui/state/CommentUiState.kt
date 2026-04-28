package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui.state

data class CommentUiState(
    val commentId: Long,
    var isExpanded: Boolean = false,
    var visibleReplyCount: Int = 0,
    var hasMoreReply: Boolean = false
) {
    fun expand(totalReplyCount: Int, pageSize: Int) {
        isExpanded = true
        visibleReplyCount = totalReplyCount.coerceAtMost(pageSize) //限制不超过最大值pageSize
        hasMoreReply = visibleReplyCount < totalReplyCount
    }

    fun expandAddReply() {
        isExpanded = true
        visibleReplyCount = visibleReplyCount + 1
    }

    fun loadMore(totalReplyCount: Int, pageSize: Int) {
        isExpanded = true
        visibleReplyCount = (visibleReplyCount + pageSize).coerceAtMost(totalReplyCount)
        hasMoreReply = visibleReplyCount < totalReplyCount
    }

    fun collapse() {
        isExpanded = false
        visibleReplyCount = 0
        hasMoreReply = false
    }
}
