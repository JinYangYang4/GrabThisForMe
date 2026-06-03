package com.example.grabthisforme.activity.informationFragment.adapter

import com.example.grabthisforme.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.ItemContactBinding
import com.example.grabthisforme.databinding.ItemGroupHeaderBinding
import com.example.grabthisforme.model.friendAndGroup.ContactItem

class AllFriendOrGroupRecyclerViewAdapter(
    private val onItemClickListener: (ContactItem) -> Unit
) : ListAdapter<ContactItem, RecyclerView.ViewHolder>(ContactItemDiffCallback()) {

    companion object {
        const val TYPE_FRIEND_HEADER = 0
        const val TYPE_CONTACT = 1
        const val TYPE_GROUP_HEADER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FRIEND_HEADER -> FriendHeaderViewHolder.inflateFrom(parent)
            TYPE_CONTACT -> ContactViewHolder.inflateFrom(parent)
            TYPE_GROUP_HEADER -> GroupHeaderViewHolder.inflateFrom(parent)
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FriendHeaderViewHolder -> holder.bind(item as ContactItem.FriendHeader)
            is ContactViewHolder -> holder.bind(item, onItemClickListener)
            is GroupHeaderViewHolder -> holder.bind(item as ContactItem.GroupHeader)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ContactItem.FriendHeader -> TYPE_FRIEND_HEADER
            is ContactItem.GroupHeader -> TYPE_GROUP_HEADER
            else -> TYPE_CONTACT
        }
    }

    // Friend Header ViewHolder
    class FriendHeaderViewHolder(val binding: ItemGroupHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): FriendHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGroupHeaderBinding.inflate(layoutInflater, parent, false)
                return FriendHeaderViewHolder(binding)
            }
        }

        fun bind(header: ContactItem.FriendHeader) {
            binding.tvGroupName.text = header.title
        }
    }

    // Contact ViewHolder (friend or group)
    class ContactViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): ContactViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemContactBinding.inflate(layoutInflater, parent, false)
                return ContactViewHolder(binding)
            }
        }

        fun bind(item: ContactItem, clickListener: (ContactItem) -> Unit) {
            when (item) {
                is ContactItem.FriendItem -> {
                    binding.tvContactName.text = item.friend.who.name
                    binding.tvContactSignature.apply {
                        text = item.friend.who.signature
                        visibility = View.VISIBLE
                    }
                    binding.vOnlineIndicator.visibility = View.VISIBLE
                    binding.flGroupBadge.visibility = View.GONE
                    binding.ivAvatar.setImageResource(R.drawable.ic_back_charactor2)
                    binding.root.setOnClickListener { clickListener(item) }
                }
                is ContactItem.GroupItem -> {
                    binding.tvContactName.text = item.group.groupName
                    binding.tvContactSignature.visibility = View.GONE
                    binding.vOnlineIndicator.visibility = View.GONE
                    binding.flGroupBadge.visibility = View.VISIBLE
                    binding.ivAvatar.setImageResource(R.drawable.ic_back_charactor2)
                    binding.root.setOnClickListener { clickListener(item) }
                }
                else -> {}
            }
        }
    }

    // Group Header ViewHolder
    class GroupHeaderViewHolder(val binding: ItemGroupHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): GroupHeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGroupHeaderBinding.inflate(layoutInflater, parent, false)
                return GroupHeaderViewHolder(binding)
            }
        }

        fun bind(header: ContactItem.GroupHeader) {
            binding.tvGroupName.text = header.title
        }
    }

    class ContactItemDiffCallback : DiffUtil.ItemCallback<ContactItem>() {
        override fun areItemsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ContactItem, newItem: ContactItem): Boolean {
            return oldItem == newItem
        }
    }
}
