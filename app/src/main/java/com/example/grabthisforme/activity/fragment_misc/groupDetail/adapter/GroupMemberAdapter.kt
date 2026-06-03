package com.example.grabthisforme.activity.fragment_misc.groupDetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.groupDetail.ui_model.GroupMemberItemUiModel
import com.example.grabthisforme.databinding.ItemGroupMemberBinding

class GroupMemberAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<GroupMemberItemUiModel, GroupMemberAdapter.ViewHolder>(GroupMemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(
        private val binding: ItemGroupMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ViewHolder(ItemGroupMemberBinding.inflate(inflater, parent, false))
            }
        }

        fun bind(item: GroupMemberItemUiModel, onItemClick: (Long) -> Unit) {
            binding.tvName.text = item.name
            binding.tvSubtitle.text = item.subtitle
            binding.tvManager.visibility = if (item.isManager) android.view.View.VISIBLE else android.view.View.GONE
            binding.root.setOnClickListener {
                onItemClick(item.userId)
            }
        }
    }

    class GroupMemberDiffCallback : DiffUtil.ItemCallback<GroupMemberItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: GroupMemberItemUiModel,
            newItem: GroupMemberItemUiModel
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: GroupMemberItemUiModel,
            newItem: GroupMemberItemUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
