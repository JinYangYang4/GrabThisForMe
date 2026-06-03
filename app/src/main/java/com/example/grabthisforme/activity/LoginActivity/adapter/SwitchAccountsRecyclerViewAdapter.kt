package com.example.grabthisforme.activity.LoginActivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.ui_model.SwitchAccountItemUiModel
import com.example.grabthisforme.databinding.RvSwitchAccountItemBinding

class SwitchAccountsRecyclerViewAdapter(
    private val onItemClick: (SwitchAccountItemUiModel) -> Unit,
    private val onIvCurrentClick: (SwitchAccountItemUiModel) -> Unit
) : ListAdapter<SwitchAccountItemUiModel, SwitchAccountsRecyclerViewAdapter.UserViewHolder>(UserDiffCallback()) {
    class UserViewHolder(private val binding: RvSwitchAccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SwitchAccountItemUiModel,onItemClick: (SwitchAccountItemUiModel) -> Unit,onIvCurrentClick: (SwitchAccountItemUiModel) -> Unit) {
            binding.apply {
                tvStatus.text = item.displayName
                tvAccount.text = item.accountText
                if (!item.avatarUrl.isNullOrEmpty()) {
                    Glide.with(ivAvatar.context)
                        .load(item.avatarUrl)
                        .circleCrop()
                        .into(ivAvatar)
                } else {
                    ivAvatar.setImageResource(R.drawable.cat)
                }
                ivCurrent.visibility = if (item.isCurrent) View.GONE else View.VISIBLE
                root.setOnClickListener {
                    onItemClick(item)
                }
                ivCurrent.setOnClickListener {
                    onIvCurrentClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RvSwitchAccountItemBinding.inflate(inflater, parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position),onItemClick,onIvCurrentClick)
    }

    class UserDiffCallback : DiffUtil.ItemCallback<SwitchAccountItemUiModel>() {
        override fun areItemsTheSame(oldItem: SwitchAccountItemUiModel, newItem: SwitchAccountItemUiModel): Boolean {
            return oldItem.userId == newItem.userId
        }
        override fun areContentsTheSame(oldItem: SwitchAccountItemUiModel, newItem: SwitchAccountItemUiModel): Boolean {
            return oldItem == newItem
        }
    }

}
