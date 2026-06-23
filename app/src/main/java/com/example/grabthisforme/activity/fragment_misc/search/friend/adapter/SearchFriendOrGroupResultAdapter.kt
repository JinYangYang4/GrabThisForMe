package com.example.grabthisforme.activity.fragment_misc.search.friend.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.search.friend.ui_model.SearchContactResultUiModel
import com.example.grabthisforme.databinding.ItemSearchContactResultBinding

class SearchFriendOrGroupResultAdapter(
    private val onItemClick: (String) -> Unit,
    private val onActionClick: (String) -> Unit
) : ListAdapter<SearchContactResultUiModel, SearchFriendOrGroupResultAdapter.ViewHolder>(
    SearchContactResultDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(
            item = getItem(position),
            onItemClick = onItemClick,
            onActionClick = onActionClick
        )
    }

    class ViewHolder(
        private val binding: ItemSearchContactResultBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                return ViewHolder(
                    ItemSearchContactResultBinding.inflate(inflater, parent, false)
                )
            }
        }

        fun bind(
            item: SearchContactResultUiModel,
            onItemClick: (String) -> Unit,
            onActionClick: (String) -> Unit
        ) {
            val context = binding.root.context
            binding.tvName.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.tvBadge.text = item.badgeText
            binding.tvStatus.text = item.statusText

            binding.flGroupBadge.visibility = if (item.isFriend) View.GONE else View.VISIBLE
            binding.vOnlineIndicator.visibility = if (item.isFriend) View.VISIBLE else View.GONE

            binding.tvAction.visibility = if (item.actionText.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.tvConnected.visibility = if (item.isConnected) View.VISIBLE else View.GONE

            if (item.isConnected) {
                binding.tvConnected.text = if (item.isFriend) "已添加" else "已加入"
                binding.tvConnected.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        if (item.isFriend) R.color.green_light else R.color.orange_ultra_light
                    )
                )
                binding.tvConnected.setTextColor(
                    ContextCompat.getColor(
                        context,
                        if (item.isFriend) R.color.green_dark else R.color.orange_dark
                    )
                )
            }

            if (!item.actionText.isNullOrBlank()) {
                binding.tvActionText.text = item.actionText
                binding.tvAction.background = ContextCompat.getDrawable(
                    context,
                    if (item.isFriend) R.drawable.bg_primary_pill_green else R.drawable.bg_primary_pill_orange
                )
                binding.tvActionText.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            }

            binding.root.setOnClickListener {
                onItemClick(item.stableId)
            }
            binding.tvAction.setOnClickListener {
                onActionClick(item.stableId)
            }
        }
    }

    class SearchContactResultDiffCallback : DiffUtil.ItemCallback<SearchContactResultUiModel>() {
        override fun areItemsTheSame(
            oldItem: SearchContactResultUiModel,
            newItem: SearchContactResultUiModel
        ): Boolean {
            return oldItem.stableId == newItem.stableId
        }

        override fun areContentsTheSame(
            oldItem: SearchContactResultUiModel,
            newItem: SearchContactResultUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
