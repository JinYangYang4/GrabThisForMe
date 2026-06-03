package com.example.grabthisforme.activity.fragment_misc.userDetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.userDetail.ui_model.UserCommonGroupItemUiModel
import com.example.grabthisforme.databinding.ItemUserCommonGroupBinding

class UserCommonGroupAdapter(
    private val onItemClick: (Long) -> Unit
) : ListAdapter<UserCommonGroupItemUiModel, UserCommonGroupAdapter.ViewHolder>(
    UserCommonGroupDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(
        private val binding: ItemUserCommonGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ViewHolder(ItemUserCommonGroupBinding.inflate(inflater, parent, false))
            }
        }

        fun bind(item: UserCommonGroupItemUiModel, onItemClick: (Long) -> Unit) {
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.root.setOnClickListener {
                onItemClick(item.groupId)
            }
        }
    }

    class UserCommonGroupDiffCallback : DiffUtil.ItemCallback<UserCommonGroupItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: UserCommonGroupItemUiModel,
            newItem: UserCommonGroupItemUiModel
        ): Boolean {
            return oldItem.groupId == newItem.groupId
        }

        override fun areContentsTheSame(
            oldItem: UserCommonGroupItemUiModel,
            newItem: UserCommonGroupItemUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
