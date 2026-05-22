package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.databinding.RvReplayItemBinding

class ReplyRecyclerViewAdapter(
    private val onReplyClick: ((Reply) -> Unit)? = null
) : ListAdapter<Reply, ReplyRecyclerViewAdapter.ReplyListViewHolder>(DiffCallback) {

    inner class ReplyListViewHolder(private val binding: RvReplayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: Reply) {
            val beCommenterName = reply.beCommenter?.name ?: "匿名"
            binding.tvReplyToTip.text = "回复 $beCommenterName:"
            binding.tvReplyUsername.text = reply.commenter?.name ?: "匿名"
            binding.tvReplyContent.text = reply.message ?: ""
            binding.tvReplyTime.text =  formatTimeLeft(reply.time)
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
            val duration =  System.currentTimeMillis() - sendTime
            if (duration <= 60*1000) return "刚刚"

            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)

            return when {
                hours > 0 -> "${hours}小时前"
                else -> "${minutes} 分钟前"
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
