package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.domain.Reply
import com.example.grabthisforme.databinding.RvReplayItemBinding

class ReplyRecyclerViewAdapter(
    private val onReplyClick: ((Reply) -> Unit)? = null
) : ListAdapter<Reply, ReplyRecyclerViewAdapter.ReplyListViewHolder>(DiffCallback) {

    inner class ReplyListViewHolder(private val binding: RvReplayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reply: Reply) {
            val beCommenterName = reply.beCommenter?.name ?: "Someone"
            binding.tvReplyToTip.text = "Reply to $beCommenterName:"
            binding.tvReplyUsername.text = reply.commenter?.name ?: "Anonymous"
            binding.tvReplyContent.text = reply.message ?: "No reply content"
            binding.tvReplyTime.text = "10 min ago"
            itemView.setOnClickListener {
                onReplyClick?.invoke(reply)
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
