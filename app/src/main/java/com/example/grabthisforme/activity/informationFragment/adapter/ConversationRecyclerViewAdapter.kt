package com.example.grabthisforme.activity.informationFragment.adapter


import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvMessageItemBinding

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.model.conversation.domain.Conversation

import java.text.SimpleDateFormat
import java.util.*

class ConversationRecyclerViewAdapter(
    private val clickListener: (conversationId: String) -> Unit
) : ListAdapter<Conversation, ConversationRecyclerViewAdapter.ViewHolder>(
    ViewHolder.MessageDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val conversation = getItem(position)
        holder.bind(conversation, clickListener)
    }

    class ViewHolder(val binding: RvMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvMessageItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(conversation: Conversation, clickListener: (String) -> Unit) {
            // 绑定聊天的用户数据
            when (val peer = conversation.conversationPeer) {
                is Conversation.ConversationPeer.Single -> {
                    binding.tvName.text = peer.user?.name
                    binding.ivAvatar.setImageResource(R.drawable.ic_back_charactor2)
                }
                is Conversation.ConversationPeer.Group -> {
                    binding.tvName.text = "Group Chat"
                    binding.ivAvatar.setImageResource(R.drawable.ic_back_charactor2)
                }
            }


            binding.tvLastMsg.text = conversation.lastMessage.content
            binding.tvTime.text = formatTimestampToDateTime(conversation.lastTime)

            // 点击事件：点击跳转到聊天界面
            binding.root.setOnClickListener {
                clickListener(conversation.conversationId)
            }
        }

        class MessageDiffCallback : DiffUtil.ItemCallback<Conversation>() {
            override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
                return oldItem.conversationId == newItem.conversationId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
                return oldItem == newItem
            }
        }

        private fun formatTimestampToDateTime(timestamp: Long): String {
            val timeInMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(timeInMillis)
            return sdf.format(date)
        }
    }
}
