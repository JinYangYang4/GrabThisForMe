package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model.Comment
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model.Reply
import com.example.grabthisforme.databinding.RvCommentItemBinding

// 常量抽离，便于统一修改
private const val REPLY_PAGE_SIZE = 3
private var increasing = 0
private const val DEFAULT_USER_NAME = "匿名用户"
private const val DEFAULT_COMMENT_CONTENT = "无评论内容"
private const val DEFAULT_TIME_TEXT = "10分钟前"

class CommentRecyclerViewAdapter(
    private val onItemClick: ((Comment,position: Int, commentId: Long) -> Unit)? = null,
    private val scrollListener: OnCommentScrollListener? = null,
    private val onReplyItemClick: ((Reply,position: Int, commentId: Long) -> Unit)? = null

) : ListAdapter<Comment, CommentRecyclerViewAdapter.CommentListViewHolder>(DiffCallback) {
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
                overScrollMode = View.OVER_SCROLL_NEVER
            }

            initAllClickEvents()
        }

        private fun initAllClickEvents() {
            binding.tvToggleReply.setOnClickListener {
                currentComment?.let { comment ->
                    toggleReplyExpansion(comment)
                    refreshReplyUI(comment)
                }
            }
            binding.tvLoadMoreReply.setOnClickListener {
                currentComment?.let { comment ->
                    loadMoreReplies(comment)
                    refreshReplyUI(comment)
                }
            }

            binding.tvCollapseExtraReply.setOnClickListener {
                currentComment?.let { comment ->
                    collapseAllReplies(comment)
                    refreshReplyUI(comment)
                }
            }
            binding.llMainComment.setOnClickListener {
                currentComment?.let { comment ->
                    onItemClick?.invoke(comment,currentPosition,comment.id)
                }
            }

        }

        fun bind(comment: Comment,position: Int) {
            this.currentComment = comment
            this.currentReplies = comment.replies ?: emptyList()
            currentPosition = position
            replyAdapter = ReplyRecyclerViewAdapter(
                comment_page = 0,
                onReplyClick = { reply->
                    onReplyItemClick?.invoke(reply, currentPosition, comment.id)
                }
            )
            binding.rvCommentReplies.adapter = replyAdapter
            binding.tvCommentUsername.text = comment.commenter?.name ?: DEFAULT_USER_NAME
            binding.tvCommentContent.text = comment.message ?: DEFAULT_COMMENT_CONTENT
            binding.tvCommentTime.text = DEFAULT_TIME_TEXT

            initReplyArea(comment)
            refreshReplyUI(comment)
        }

        private fun initReplyArea(comment: Comment) {
            val totalReplyCount = currentReplies.size
            if (totalReplyCount == 0) {
                hideAllReplyViews()
                return
            }
            binding.tvToggleReply.visibility = View.VISIBLE
            updateToggleButtonText(comment, totalReplyCount)
        }

        private fun refreshReplyUI(comment: Comment) {
            val totalReplyCount = currentReplies.size
            if (totalReplyCount == 0) {
                hideAllReplyViews()
                return
            }

            if (comment.isExpanded) {
                val showCount = comment.page.coerceAtLeast(1).coerceAtMost(totalReplyCount)
                val showReplies = currentReplies.take(showCount).toList()
                replyAdapter.submitList(showReplies)
                binding.rvCommentReplies.visibility = View.VISIBLE
                binding.tvCollapseExtraReply.visibility = View.VISIBLE
                binding.tvLoadMoreReply.visibility = if (showCount >= totalReplyCount) View.GONE else View.VISIBLE
            } else {
                replyAdapter.submitList(emptyList<Reply>().toList())
                binding.rvCommentReplies.visibility = View.GONE
                binding.tvCollapseExtraReply.visibility = View.GONE
                binding.tvLoadMoreReply.visibility = View.GONE
            }
        }

        private fun toggleReplyExpansion(comment: Comment) {
            comment.isExpanded = !comment.isExpanded
            increasing = 0
            if (comment.isExpanded) {
                comment.page = REPLY_PAGE_SIZE.coerceAtMost(currentReplies.size)
            } else {
                comment.page = 0
                binding.root.post {
                    scrollListener?.onCommentCollapse(currentPosition)
                }
            }
            updateToggleButtonText(comment, currentReplies.size)
        }

        private fun loadMoreReplies(comment: Comment) {
            increasing += 2
            increasing.coerceAtMost(10)
            val newPage = comment.page + REPLY_PAGE_SIZE + increasing
            comment.page = newPage.coerceAtMost(currentReplies.size)
        }

        private fun collapseAllReplies(comment: Comment) {
            comment.isExpanded = false
            comment.page = 0
            updateToggleButtonText(comment, currentReplies.size)
            binding.root.post {
                scrollListener?.onCommentCollapse(currentPosition)
            }
        }

        private fun updateToggleButtonText(comment: Comment, totalReplyCount: Int) {
            if (comment.isExpanded) {
                binding.tvToggleReply.visibility = View.GONE
            } else {
                binding.tvToggleReply.visibility = View.VISIBLE
                binding.tvToggleReply.text =  "展开回复($totalReplyCount)"
            }
        }

        private fun hideAllReplyViews() {
            binding.tvToggleReply.visibility = View.GONE
            binding.rvCommentReplies.visibility = View.GONE
            binding.tvCollapseExtraReply.visibility = View.GONE
            binding.tvLoadMoreReply.visibility = View.GONE
            replyAdapter.submitList(emptyList<Reply>().toList())
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commenter?.name == newItem.commenter?.name &&
                    oldItem.message == newItem.message &&
                    oldItem.replies?.size == newItem.replies?.size
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
        val comment = getItem(position)
        holder.bind(comment,position)
    }

}