package com.example.grabthisforme.activity.fragment_misc.searchFragment.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.databinding.RvSearchRecommendationItemBinding

class SearchRecyclerViewAdapter(
    private val clickListener: (SearchContent) -> Unit
) : ListAdapter<SearchContent, SearchRecyclerViewAdapter.ViewHolder>(
    SearchHistoryDiffItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position, clickListener)
    }

    class ViewHolder(
        private val binding: RvSearchRecommendationItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvSearchRecommendationItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            search: SearchContent,
            position: Int,
            clickListener: (SearchContent) -> Unit
        ) {
            val context = binding.root.context
            val useWarmBadge = position < 3

            binding.tvTag.text = search.content
            binding.tvIndex.text = (position + 1).toString()
            binding.tvState.text = if (useWarmBadge) "HOT" else "GO"

            binding.tvState.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (useWarmBadge) R.color.orange_ultra_light else R.color.gray_light
                )
            )
            binding.tvState.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (useWarmBadge) R.color.orange_dark else R.color.gray_700
                )
            )
            binding.tvIndex.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (useWarmBadge) R.color.orange_light else R.color.green_light
                )
            )
            binding.tvIndex.setTextColor(
                ContextCompat.getColor(
                    context,
                    if (useWarmBadge) R.color.orange_dark else R.color.green_dark
                )
            )
            binding.root.setOnClickListener {
                clickListener.invoke(search)
            }
        }
    }

    class SearchHistoryDiffItemCallback : DiffUtil.ItemCallback<SearchContent>() {
        override fun areItemsTheSame(oldItem: SearchContent, newItem: SearchContent): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SearchContent, newItem: SearchContent): Boolean {
            return oldItem.content == newItem.content &&
                oldItem.search_time == newItem.search_time &&
                oldItem.id == newItem.id
        }
    }
}
