package com.example.grabthisforme.activity.informationFragment.adapter

import com.example.grabthisforme.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.ItemFriendBinding
import com.example.grabthisforme.databinding.ItemGroupBinding
import com.example.grabthisforme.databinding.ItemGroupHeaderBinding
import com.example.grabthisforme.model.friendAndGroup.ContactItem
class AllFriendOrGroupRecyclerViewAdapter(
    private val onItemClickListener: (Long) -> Unit
) : ListAdapter<ContactItem, RecyclerView.ViewHolder>(ContactItemDiffCallback()) {

    companion object {
        const val TYPE_FRIEND_HEADER = 0
        const val TYPE_FRIEND_ITEM = 1
        const val TYPE_GROUP_HEADER = 2
        const val TYPE_GROUP_ITEM = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FRIEND_HEADER -> FriendHeaderViewHolder.inflateFrom(parent)
            TYPE_FRIEND_ITEM -> FriendViewHolder.inflateFrom(parent)
            TYPE_GROUP_HEADER -> GroupHeaderViewHolder.inflateFrom(parent)
            TYPE_GROUP_ITEM -> GroupViewHolder.inflateFrom(parent)
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is FriendHeaderViewHolder -> holder.bind(item as ContactItem.FriendHeader)
            is FriendViewHolder -> holder.bind(item as ContactItem.FriendItem, onItemClickListener)
            is GroupHeaderViewHolder -> holder.bind(item as ContactItem.GroupHeader)
            is GroupViewHolder -> holder.bind(item as ContactItem.GroupItem, onItemClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ContactItem.FriendHeader -> TYPE_FRIEND_HEADER
            is ContactItem.FriendItem -> TYPE_FRIEND_ITEM
            is ContactItem.GroupHeader -> TYPE_GROUP_HEADER
            is ContactItem.GroupItem -> TYPE_GROUP_ITEM
            else -> TYPE_FRIEND_HEADER
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

    // Friend ViewHolder
    class FriendViewHolder(val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): FriendViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemFriendBinding.inflate(layoutInflater, parent, false)
                return FriendViewHolder(binding)
            }
        }

        fun bind(friendItem: ContactItem.FriendItem, clickListener: (Long) -> Unit) {
            binding.tvFriendName.text = friendItem.friend.who.name
            binding.tvFriendSignature.text = friendItem.friend.who.signature
            binding.ivAvatar.setImageResource(R.drawable.ic_back_charactor2)
            binding.root.setOnClickListener {
                clickListener(friendItem.friend.who.id)
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

    // Group ViewHolder
    class GroupViewHolder(val binding: ItemGroupBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): GroupViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemGroupBinding.inflate(layoutInflater, parent, false)
                return GroupViewHolder(binding)
            }
        }

        fun bind(groupItem: ContactItem.GroupItem, clickListener: (Long) -> Unit) {
            binding.tvGroupName.text = groupItem.group.groupName
            binding.ivGroupAvatar.setImageResource(R.drawable.ic_back_charactor2) // 图片加载
            binding.root.setOnClickListener {
                clickListener(groupItem.group.groupId)
            }
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
