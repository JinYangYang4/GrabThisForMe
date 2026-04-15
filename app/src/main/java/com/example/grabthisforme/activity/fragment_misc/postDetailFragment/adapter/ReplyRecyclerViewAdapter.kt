package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.model.Reply
import com.example.grabthisforme.databinding.RvReplayItemBinding


class ReplyRecyclerViewAdapter(
    private val onReplyClick: ((Reply) -> Unit)? = null,comment_page : Int,
) : ListAdapter<Reply, ReplyRecyclerViewAdapter.ReplyListViewHolder>(DiffCallback) {

    inner class ReplyListViewHolder(private val binding: RvReplayItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(reply: Reply) {
            val beCommenterName = reply.beCommenter?.name ?: "某人"
            binding.tvReplyToTip.text = "回复 $beCommenterName："
            binding.tvReplyUsername.text = reply.commenter?.name ?: "匿名用户"
            binding.tvReplyContent.text = reply.message ?: "无回复内容"
            binding.tvReplyTime.text = "10分钟前"
            itemView.setOnClickListener {
                onReplyClick?.invoke(reply)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Reply>() {
        override fun areItemsTheSame(oldItem: Reply, newItem: Reply): Boolean {
            Log.d("test11", "areItemsTheSame: 旧ID=${oldItem.id} 新ID=${newItem.id}") // 优化日志，打印ID
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
            val isSame = oldItem == newItem
            Log.d("test11", "areContentsTheSame: 内容是否相同=$isSame")
            return isSame
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyListViewHolder {
        // 修复：布局绑定类拼写错误+正确初始化
        val binding = RvReplayItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReplyListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyListViewHolder, position: Int) {
        val reply = getItem(position)
        holder.bind(reply)
    }
}