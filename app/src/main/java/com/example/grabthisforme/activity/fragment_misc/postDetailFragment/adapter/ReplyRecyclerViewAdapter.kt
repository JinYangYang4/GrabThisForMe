package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.LocalSendStatus
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.databinding.RvReplayItemBinding

private const val DEFAULT_REPLY_USER_NAME = "\u533f\u540d"
private const val DEFAULT_REPLY_PREFIX = "\u56de\u590d"

class ReplyRecyclerViewAdapter(
    private val onReplyClick: ((Reply) -> Unit)? = null
) : ListAdapter<Reply, ReplyRecyclerViewAdapter.ReplyListViewHolder>(DiffCallback) {

    inner class ReplyListViewHolder(private val binding: RvReplayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: Reply) {
            val beCommenterName = reply.beCommenter?.name ?: DEFAULT_REPLY_USER_NAME
            binding.tvReplyToTip.text = "$DEFAULT_REPLY_PREFIX $beCommenterName:"
            binding.tvReplyUsername.text = reply.commenter?.name ?: DEFAULT_REPLY_USER_NAME
            binding.tvReplyContent.text = reply.message ?: ""
            binding.tvReplyTime.text = buildReplyMetaText(reply)
            Glide.with(binding.root.context)
                .load(reply.commenter?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivReplyAvatar)

            itemView.setOnClickListener {
                onReplyClick?.invoke(reply)
            }
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

        private fun buildReplyMetaText(reply: Reply): String {
            return when (reply.sendStatus) {
                LocalSendStatus.NONE -> formatTimeLeft(reply.time)
                LocalSendStatus.SENDING -> "\u53d1\u9001\u4e2d"
                LocalSendStatus.FAILED -> "\u53d1\u9001\u5931\u8d25"
                LocalSendStatus.SUCCESS -> "\u53d1\u9001\u6210\u529f"
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Reply>() {
        override fun areItemsTheSame(oldItem: Reply, newItem: Reply): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyListViewHolder {
        val binding = RvReplayItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReplyListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
