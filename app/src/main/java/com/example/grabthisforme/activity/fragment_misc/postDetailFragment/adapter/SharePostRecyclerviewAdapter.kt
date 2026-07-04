package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvShareItemBinding
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.SelectableItem

class SharePostRecyclerviewAdapter(
    private val onItemClickListener: (Long) -> Unit
) : ListAdapter<SelectableItem, RecyclerView.ViewHolder>(SelectableItemDiffCallback()) {

    companion object {
        const val TYPE_SELECTABLE_FRIEND = 0
        const val TYPE_SELECTABLE_GROUP = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SELECTABLE_FRIEND -> SelectableFriendViewHolder.inflateFrom(parent)
            TYPE_SELECTABLE_GROUP -> SelectableGroupViewHolder.inflateFrom(parent)
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SelectableFriendViewHolder -> {
                holder.bind(getItem(position) as SelectableItem.SelectableFriend, onItemClickListener)
            }

            is SelectableGroupViewHolder -> {
                holder.bind(getItem(position) as SelectableItem.SelectableGroup, onItemClickListener)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SelectableItem.SelectableFriend -> TYPE_SELECTABLE_FRIEND
            is SelectableItem.SelectableGroup -> TYPE_SELECTABLE_GROUP
        }
    }

    fun toggleItemSelection(position: Int) {
        val currentList = currentList.toMutableList()
        val oldItem = currentList[position]
        val updatedItem = when (oldItem) {
            is SelectableItem.SelectableFriend -> oldItem.copy(isSelected = !oldItem.isSelected)
            is SelectableItem.SelectableGroup -> oldItem.copy(isSelected = !oldItem.isSelected)
        }
        currentList[position] = updatedItem
        submitList(currentList)
    }

    class SelectableFriendViewHolder(
        private val binding: RvShareItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): SelectableFriendViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvShareItemBinding.inflate(layoutInflater, parent, false)
                return SelectableFriendViewHolder(binding)
            }
        }

        fun bind(item: SelectableItem.SelectableFriend, clickListener: (Long) -> Unit) {
            val friend: Friend = item.friend
            binding.tvName.text = friend.who.name
            binding.ivHead.setImageResource(com.example.grabthisforme.R.drawable.ic_app_icon)
            binding.flSelectedMask.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            binding.flSelectedIcon.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                clickListener(friend.friendId)
                (bindingAdapter as? SharePostRecyclerviewAdapter)?.toggleItemSelection(position)
            }
        }
    }

    class SelectableGroupViewHolder(
        private val binding: RvShareItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): SelectableGroupViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvShareItemBinding.inflate(layoutInflater, parent, false)
                return SelectableGroupViewHolder(binding)
            }
        }

        fun bind(item: SelectableItem.SelectableGroup, clickListener: (Long) -> Unit) {
            val group: Group = item.group
            binding.tvName.text = group.groupName
            binding.ivHead.setImageResource(com.example.grabthisforme.R.drawable.ic_app_icon)
            binding.flSelectedMask.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            binding.flSelectedIcon.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                clickListener(group.groupId)
                (bindingAdapter as? SharePostRecyclerviewAdapter)?.toggleItemSelection(position)
            }
        }
    }

    class SelectableItemDiffCallback : DiffUtil.ItemCallback<SelectableItem>() {
        override fun areItemsTheSame(
            oldItem: SelectableItem,
            newItem: SelectableItem
        ): Boolean {
            return when {
                oldItem is SelectableItem.SelectableFriend && newItem is SelectableItem.SelectableFriend ->
                    oldItem.friend.friendId == newItem.friend.friendId

                oldItem is SelectableItem.SelectableGroup && newItem is SelectableItem.SelectableGroup ->
                    oldItem.group.groupId == newItem.group.groupId

                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: SelectableItem,
            newItem: SelectableItem
        ): Boolean {
            return oldItem == newItem
        }
    }

    fun getSelectedFriendOrGroup(): List<Long> {
        return currentList.filter { item ->
            when (item) {
                is SelectableItem.SelectableFriend -> item.isSelected
                is SelectableItem.SelectableGroup -> item.isSelected
            }
        }.map { item ->
            when (item) {
                is SelectableItem.SelectableFriend -> item.friend.friendId
                is SelectableItem.SelectableGroup -> item.group.groupId
            }
        }
    }
}
