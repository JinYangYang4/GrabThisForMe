package com.example.grabthisforme.ui.goods.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvConditionItemBinding

open class FilterChipRecyclerViewAdapter<T : Any>(
    private val idProvider: (T) -> Long,
    private val labelProvider: (T) -> String,
    private val selectedProvider: (T) -> Boolean,
    private val clickListener: (item: T) -> Unit
) : ListAdapter<T, FilterChipRecyclerViewAdapter<T>.ViewHolder>(DiffCallback(idProvider)) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RvConditionItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: RvConditionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: T) {
            binding.tvCondition.text = labelProvider(item)
            updateSelectedStyle(selectedProvider(item))
            itemView.setOnClickListener { clickListener(item) }
        }

        private fun updateSelectedStyle(isSelected: Boolean) {
            if (isSelected) {
                binding.root.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.bg_primary_pill_green)
                binding.tvCondition.setTextColor(
                    ContextCompat.getColor(itemView.context, android.R.color.white)
                )
            } else {
                binding.root.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.bg_search_soft_chip)
                binding.tvCondition.setTextColor(
                    ContextCompat.getColor(itemView.context, R.color.gray_700)
                )
            }
        }
    }

    class DiffCallback<T : Any>(
        private val idProvider: (T) -> Long
    ) : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return idProvider(oldItem) == idProvider(newItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }
}
