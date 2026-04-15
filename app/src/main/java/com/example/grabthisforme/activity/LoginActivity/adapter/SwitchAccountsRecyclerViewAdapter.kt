package com.example.grabthisforme.activity.LoginActivity.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvSwitchAccountItemBinding
import com.example.grabthisforme.model.user.User

class SwitchAccountsRecyclerViewAdapter(
    private val onItemClick: (User) -> Unit,
    private val onIvCurrentClick: (User) -> Unit
) : ListAdapter<User, SwitchAccountsRecyclerViewAdapter.UserViewHolder>(UserDiffCallback()) {
    class UserViewHolder(private val binding: RvSwitchAccountItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: User,onItemClick: (User) -> Unit,onIvCurrentClick: (User) -> Unit) {
            val user = item
            binding.apply {

                tvStatus.text = user.name
                tvAccount.text = user.id.toString()
                if (user.headPic.isNotEmpty()) {
                    Glide.with(ivAvatar.context)
                        .load(user.headPic)
                        .circleCrop()
                        .into(ivAvatar)
                } else {
                    ivAvatar.setImageResource(R.drawable.cat)
                }
                ivCurrent.visibility = if (item.isCurrent) View.GONE else View.VISIBLE
                root.setOnClickListener {
                    onItemClick(user)
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

    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name
        }
    }

}