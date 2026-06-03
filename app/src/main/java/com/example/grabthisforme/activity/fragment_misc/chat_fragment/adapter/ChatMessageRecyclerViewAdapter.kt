package com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.ui_model.MessageUiModel
import com.example.grabthisforme.databinding.RvChatMessageItemBinding
import com.example.grabthisforme.model.message.domain.Message

typealias OnImageClick = (MessageUiModel) -> Unit
typealias OnPeerAvatarClick = (Long) -> Unit

class ChatMessageRecyclerViewAdapter(
    private val clickListener: (MessageUiModel) -> Unit,
    private val onImageClick: OnImageClick,
    private val onPeerAvatarClick: OnPeerAvatarClick
) : ListAdapter<MessageUiModel, ChatMessageRecyclerViewAdapter.ViewHolder>(MessageDiffCallback()) {

    inner class ViewHolder(private val binding: RvChatMessageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: MessageUiModel, clickListener: (MessageUiModel) -> Unit) {
            if (message.isMine) {
                bindMineMessage(message)
            } else {
                bindPeerMessage(message)
            }

            if (message.showTime) {
                binding.llTime.visibility = View.VISIBLE
                binding.tvTime.text = message.timeText
            } else {
                binding.llTime.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                clickListener.invoke(message)
            }
        }

        private fun bindMineMessage(message: MessageUiModel) {
            Glide.with(binding.root.context)
                .load(message.senderAvatarUrl)
                .placeholder(R.drawable.ic_back_charactor2)
                .error(R.drawable.ic_back_charactor2)
                .into(binding.ivSendAvatar)

            if (message.type == Message.MessageType.IMAGE) {
                binding.ivSendImage.visibility = View.VISIBLE
                binding.tvSendContent.visibility = View.GONE
                Glide.with(binding.root.context)
                    .load(message.mediaUrl)
                    .placeholder(R.drawable.ic_back_charactor2)
                    .error(R.drawable.ic_back_charactor2)
                    .into(binding.ivSendImage)
                binding.ivSendImage.setOnClickListener {
                    if (!message.mediaUrl.isNullOrBlank()) {
                        onImageClick.invoke(message)
                    }
                }
            } else {
                binding.ivSendImage.visibility = View.GONE
                binding.tvSendContent.visibility = View.VISIBLE
                binding.tvSendContent.text = message.content.orEmpty()
                binding.ivSendImage.setOnClickListener(null)
            }
            binding.llSendMessage.visibility = View.VISIBLE
            binding.llReceiveMessage.visibility = View.GONE
        }

        private fun bindPeerMessage(message: MessageUiModel) {
            Glide.with(binding.root.context)
                .load(message.senderAvatarUrl)
                .placeholder(R.drawable.ic_back_charactor2)
                .error(R.drawable.ic_back_charactor2)
                .into(binding.ivReceiveAvatar)
            binding.ivReceiveAvatar.setOnClickListener {
                onPeerAvatarClick.invoke(message.senderId)
            }

            if (message.type == Message.MessageType.IMAGE) {
                binding.ivReceiveImage.visibility = View.VISIBLE
                binding.tvReceiveContent.visibility = View.GONE
                Glide.with(binding.root.context)
                    .load(message.mediaUrl)
                    .placeholder(R.drawable.ic_back_charactor2)
                    .error(R.drawable.ic_back_charactor2)
                    .into(binding.ivReceiveImage)
                binding.ivReceiveImage.setOnClickListener {
                    if (!message.mediaUrl.isNullOrBlank()) {
                        onImageClick.invoke(message)
                    }
                }
            } else {
                binding.ivReceiveImage.visibility = View.GONE
                binding.tvReceiveContent.visibility = View.VISIBLE
                binding.tvReceiveContent.text = message.content.orEmpty()
                binding.ivReceiveImage.setOnClickListener(null)
            }
            binding.llReceiveMessage.visibility = View.VISIBLE
            binding.llSendMessage.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvChatMessageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MessageUiModel>() {
        override fun areItemsTheSame(oldItem: MessageUiModel, newItem: MessageUiModel): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: MessageUiModel, newItem: MessageUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
