package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.LocalSendStatus
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.ui.state.CommentUiState
import com.example.grabthisforme.databinding.RvCommentItemBinding

private const val DEFAULT_USER_NAME = "\u533f\u540d"
private const val DEFAULT_COMMENT_CONTENT = "\u6682\u65e0\u8bc4\u8bba\u5185\u5bb9"

class CommentRecyclerViewAdapter(
    private val onItemClick: ((Comment, Int, Long) -> Unit)? = null,
    private val onReplyItemClick: ((Reply, Int, Long) -> Unit)? = null,
    private val onLoadMoreReply: ((Comment, Int, Int) -> Unit)? = null
) : ListAdapter<Comment, CommentRecyclerViewAdapter.CommentListViewHolder>(DiffCallback) {

    private val commentUiStateMap = mutableMapOf<Long, CommentUiState>()

    fun expandComment(commentId: Long) {
        commentUiStateMap.getOrPut(commentId) {
            CommentUiState(commentId = commentId)
        }.expandAddReply()
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
                overScrollMode = View.OVER_SCROLL_NEVER
            }

            initAllClickEvents()
        }

        private fun initAllClickEvents() {
            binding.tvToggleReply.setOnClickListener {
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
            Glide.with(binding.root.context)
                .load(comment.commenter?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivCommentAvatar)
            binding.rvCommentReplies.adapter = replyAdapter
            binding.tvCommentUsername.text = comment.commenter?.name ?: DEFAULT_USER_NAME
            binding.tvCommentProvince.text = comment.commenterProvince
            binding.tvCommentProvince.visibility =
                if (comment.commenterProvince.isBlank()) View.GONE else View.VISIBLE
            binding.tvCommentContent.text = comment.message ?: DEFAULT_COMMENT_CONTENT
            binding.tvCommentTime.text = buildCommentMetaText(comment)
            refreshReplyUI(state)
        }

        private fun formatTimeLeft(sendTime: Long): String {
            val duration = System.currentTimeMillis() - sendTime
            if (duration <= 60 * 1000) return "\u521a\u521a"

            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)

            return when {
                hours > 0 -> "${hours}\u5c0f\u65f6\u524d"
                else -> "${minutes} \u5206\u949f\u524d"
            }
        }

        private fun getOrCreateCommentUiState(comment: Comment): CommentUiState {
            return commentUiStateMap.getOrPut(comment.id) {
                CommentUiState(commentId = comment.id)
            }
        }

        private fun buildCommentMetaText(comment: Comment): String {
            return when (comment.sendStatus) {
                LocalSendStatus.NONE -> formatTimeLeft(comment.time)
                LocalSendStatus.SENDING -> "\u53d1\u9001\u4e2d"
                LocalSendStatus.FAILED -> "\u53d1\u9001\u5931\u8d25"
                LocalSendStatus.SUCCESS -> "\u53d1\u9001\u6210\u529f"
            }
        }

        private fun refreshReplyUI(state: CommentUiState) {
            val totalReplyCount = currentComment?.replyCount ?: currentReplies.size
            if (totalReplyCount == 0) {
                hideAllReplyViews()
                return
            }

            if (state.isExpanded) {
                val showCount = state.visibleReplyCount.coerceAtLeast(1)
                    .coerceAtMost(totalReplyCount)
                replyAdapter.submitList(currentReplies.take(showCount))
                binding.rvCommentReplies.visibility = if (currentReplies.isEmpty()) View.GONE else View.VISIBLE
                binding.tvCollapseExtraReply.visibility = View.VISIBLE
                binding.tvLoadMoreReply.visibility = if (showCount >= totalReplyCount) View.GONE else View.VISIBLE
                binding.tvToggleReply.visibility = View.GONE
            } else {
                replyAdapter.submitList(emptyList())
                binding.rvCommentReplies.visibility = View.GONE
                binding.tvCollapseExtraReply.visibility = View.GONE
                binding.tvLoadMoreReply.visibility = View.GONE
                binding.tvToggleReply.visibility = View.VISIBLE
                binding.tvToggleReply.text = "\u5c55\u5f00\u56de\u590d($totalReplyCount)"
            }
        }

        private fun toggleReplyExpansion(comment: Comment) {
            val state = getOrCreateCommentUiState(comment)
            if (state.isExpanded) {
                state.collapse()
            } else {
                val targetCount = state.nextLoadTargetCount()
                state.expand(comment.replyCount, targetCount)
                onLoadMoreReply?.invoke(comment, currentPosition, targetCount)
            }
            refreshReplyUI(state)
        }

        private fun loadMoreReplies(comment: Comment) {
            val state = getOrCreateCommentUiState(comment)
            val targetCount = state.nextLoadTargetCount()
            val increment = (targetCount - state.visibleReplyCount).coerceAtLeast(0)
            state.loadMore(comment.replyCount, increment)
            onLoadMoreReply?.invoke(comment, currentPosition, targetCount)
            refreshReplyUI(state)
        }

        private fun collapseAllReplies(comment: Comment) {
            val state = getOrCreateCommentUiState(comment)
            state.collapse()
            refreshReplyUI(state)
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
