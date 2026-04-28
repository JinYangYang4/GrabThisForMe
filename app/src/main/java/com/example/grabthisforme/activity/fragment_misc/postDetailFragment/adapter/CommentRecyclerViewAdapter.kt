package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui.state.CommentUiState
import com.example.grabthisforme.databinding.RvCommentItemBinding

private const val REPLY_PAGE_SIZE = 3
private const val DEFAULT_USER_NAME = "Anonymous"
private const val DEFAULT_COMMENT_CONTENT = "No comment content"
private const val DEFAULT_TIME_TEXT = "10 min ago"

class CommentRecyclerViewAdapter(
    private val onItemClick: ((Comment, Int, Long) -> Unit)? = null,
    private val scrollListener: OnCommentScrollListener? = null,
    private val onReplyItemClick: ((Reply, Int, Long) -> Unit)? = null
) : ListAdapter<Comment, CommentRecyclerViewAdapter.CommentListViewHolder>(DiffCallback) {

    private val commentUiStateMap = mutableMapOf<Long, CommentUiState>()

    fun expandComment(commentId: Long) {
        commentUiStateMap.getOrPut(commentId) {
            CommentUiState(commentId = commentId)
        }.expandAddReply()
    }

    interface OnCommentScrollListener {
        fun onCommentCollapse(position: Int)
    }

    inner class CommentListViewHolder(private val binding: RvCommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentPosition = -1
        private lateinit var replyAdapter: ReplyRecyclerViewAdapter
        private var currentReplies: List<Reply> = emptyList()
        private var currentComment: Comment? = null

        init {
            binding.rvCommentReplies.run {
                layoutManager = LinearLayoutManager(binding.root.context)
                isNestedScrollingEnabled = false
                overScrollMode = View.OVER_SCROLL_NEVER  //完全禁用过度滑动特效，界面更干净
            }

            initAllClickEvents()
        }

        private fun initAllClickEvents() {
            binding.tvToggleReply.setOnClickListener {   //展开回复
                currentComment?.let { comment ->
                    toggleReplyExpansion(comment)
                }
            }
            binding.tvLoadMoreReply.setOnClickListener {
                currentComment?.let { comment ->
                    loadMoreReplies(comment)
                }
            }

            binding.tvCollapseExtraReply.setOnClickListener {
                currentComment?.let { comment ->
                    collapseAllReplies(comment)
                }
            }
            binding.llMainComment.setOnClickListener {
                currentComment?.let { comment ->
                    onItemClick?.invoke(comment, currentPosition, comment.id)
                }
            }
        }

        fun bind(comment: Comment, position: Int) {
            currentComment = comment
            currentReplies = comment.replies
            currentPosition = position
            val state = getOrCreateCommentUiState(comment)

            replyAdapter = ReplyRecyclerViewAdapter(
                onReplyClick = { reply ->
                    onReplyItemClick?.invoke(reply, currentPosition, comment.id)
                }
            )
            binding.rvCommentReplies.adapter = replyAdapter
            binding.tvCommentUsername.text = comment.commenter?.name ?: DEFAULT_USER_NAME
            binding.tvCommentContent.text = comment.message ?: DEFAULT_COMMENT_CONTENT
            binding.tvCommentTime.text = DEFAULT_TIME_TEXT

            refreshReplyUI(state)
        }

        private fun getOrCreateCommentUiState(comment: Comment): CommentUiState {
            return commentUiStateMap.getOrPut(comment.id) {
                CommentUiState(commentId = comment.id)
            }
        }

        private fun refreshReplyUI(state: CommentUiState) {
            val totalReplyCount = currentReplies.size
            if (totalReplyCount == 0) {
                hideAllReplyViews()
                return
            }

            if (state.isExpanded) {
                val showCount = state.visibleReplyCount.coerceAtLeast(1)
                    .coerceAtMost(totalReplyCount)
                replyAdapter.submitList(currentReplies.take(showCount))
                binding.rvCommentReplies.visibility = View.VISIBLE
                binding.tvCollapseExtraReply.visibility = View.VISIBLE
                binding.tvLoadMoreReply.visibility = if (showCount >= totalReplyCount) View.GONE else View.VISIBLE
                binding.tvToggleReply.visibility = View.GONE
            } else {
                replyAdapter.submitList(emptyList())
                binding.rvCommentReplies.visibility = View.GONE
                binding.tvCollapseExtraReply.visibility = View.GONE
                binding.tvLoadMoreReply.visibility = View.GONE
                binding.tvToggleReply.visibility = View.VISIBLE
                binding.tvToggleReply.text = "Expand replies($totalReplyCount)"
            }
        }

        private fun toggleReplyExpansion(comment: Comment) {     //切换展开/关闭回复
            val state = getOrCreateCommentUiState(comment)
            if (state.isExpanded) {
                state.collapse()
                binding.root.post {
                    scrollListener?.onCommentCollapse(currentPosition)
                }
            } else {
                state.expand(currentReplies.size, REPLY_PAGE_SIZE)
            }
            refreshReplyUI(state)
        }

        private fun loadMoreReplies(comment: Comment) {
            val state = getOrCreateCommentUiState(comment)
            state.loadMore(currentReplies.size, REPLY_PAGE_SIZE)
            refreshReplyUI(state)
        }

        private fun collapseAllReplies(comment: Comment) {
            val state = getOrCreateCommentUiState(comment)
            state.collapse()
            refreshReplyUI(state)
            binding.root.post {
                scrollListener?.onCommentCollapse(currentPosition)
            }
        }

        private fun hideAllReplyViews() {
            binding.tvToggleReply.visibility = View.GONE
            binding.rvCommentReplies.visibility = View.GONE
            binding.tvCollapseExtraReply.visibility = View.GONE
            binding.tvLoadMoreReply.visibility = View.GONE
            replyAdapter.submitList(emptyList())
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentListViewHolder {
        val binding = RvCommentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentListViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
}
