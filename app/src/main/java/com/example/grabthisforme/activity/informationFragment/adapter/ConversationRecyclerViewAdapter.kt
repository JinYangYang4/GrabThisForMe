package com.example.grabthisforme.activity.informationFragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.informationFragment.ui_model.ConversationListItemUiModel
import com.example.grabthisforme.databinding.RvMessageItemBinding

class ConversationRecyclerViewAdapter(
    private val clickListener: (conversationId: String) -> Unit,
    private val longClickListener: (anchor: View, item: ConversationListItemUiModel) -> Unit
) : ListAdapter<ConversationListItemUiModel, ConversationRecyclerViewAdapter.ViewHolder>(
    ViewHolder.MessageDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflateFrom(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, longClickListener)
    }

    class ViewHolder(val binding: RvMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvMessageItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: ConversationListItemUiModel,
            clickListener: (String) -> Unit,
            longClickListener: (anchor: View, item: ConversationListItemUiModel) -> Unit
        ) {
            binding.tvName.text = item.title
            binding.tvLastMsg.text = item.lastMessageText
            binding.tvTime.text = item.timeText

            if (item.showUnreadBadge) {
                binding.unreadBadge.visibility = View.VISIBLE
                binding.unreadBadge.text = item.unreadCount.coerceAtMost(99).toString()
            } else {
                binding.unreadBadge.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(item.avatarUrl)
                .placeholder(R.drawable.ic_back_charactor2)
                .error(R.drawable.ic_back_charactor2)
                .into(binding.ivAvatar)

            binding.root.setOnClickListener {
                clickListener(item.conversationId)
            }
            binding.root.setOnLongClickListener { anchor ->
                longClickListener(anchor, item)
                true
            }
        }

        class MessageDiffCallback : DiffUtil.ItemCallback<ConversationListItemUiModel>() {
            override fun areItemsTheSame(
                oldItem: ConversationListItemUiModel,
                newItem: ConversationListItemUiModel
            ): Boolean {
                return oldItem.conversationId == newItem.conversationId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: ConversationListItemUiModel,
                newItem: ConversationListItemUiModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
