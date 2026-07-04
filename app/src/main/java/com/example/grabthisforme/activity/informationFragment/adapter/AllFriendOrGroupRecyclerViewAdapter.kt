package com.example.grabthisforme.activity.informationFragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.ItemContactBinding
import com.example.grabthisforme.databinding.ItemGroupHeaderBinding
import com.example.grabthisforme.model.friendAndGroup.ContactItem

class AllFriendOrGroupRecyclerViewAdapter(
    private val onItemClickListener: (ContactItem) -> Unit
) : ListAdapter<ContactItem, RecyclerView.ViewHolder>(ContactItemDiffCallback()) {

    companion object {
        private const val TYPE_FRIEND_HEADER = 0
        private const val TYPE_CONTACT = 1
        private const val TYPE_GROUP_HEADER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_FRIEND_HEADER -> HeaderViewHolder.inflateFrom(parent)
            TYPE_GROUP_HEADER -> HeaderViewHolder.inflateFrom(parent)
            TYPE_CONTACT -> ContactViewHolder.inflateFrom(parent)
            else -> throw IllegalArgumentException("Unknown viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> when (item) {
                is ContactItem.FriendHeader -> holder.bind(item.title)
                is ContactItem.GroupHeader -> holder.bind(item.title)
                else -> Unit
            }

            is ContactViewHolder -> holder.bind(item, onItemClickListener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ContactItem.FriendHeader -> TYPE_FRIEND_HEADER
            is ContactItem.GroupHeader -> TYPE_GROUP_HEADER
            else -> TYPE_CONTACT
        }
    }

    class HeaderViewHolder(
        private val binding: ItemGroupHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): HeaderViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return HeaderViewHolder(
                    ItemGroupHeaderBinding.inflate(inflater, parent, false)
                )
            }
        }

        fun bind(title: String) {
            binding.tvGroupName.text = title
        }
    }

    class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): ContactViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ContactViewHolder(ItemContactBinding.inflate(inflater, parent, false))
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
                }

                is ContactItem.GroupItem -> {
                    binding.tvContactName.text = item.group.groupName
                    binding.tvContactSignature.visibility = View.GONE
                    binding.vOnlineIndicator.visibility = View.GONE
                    binding.flGroupBadge.visibility = View.VISIBLE
                    binding.ivAvatar.setImageResource(R.drawable.ic_back_charactor2)
                }

                else -> return
            }
            binding.root.setOnClickListener { clickListener(item) }
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
