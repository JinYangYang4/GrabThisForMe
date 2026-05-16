package com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvCategorySortItemBinding

class CategorySortRecyclerViewAdapter :
    ListAdapter<String, CategorySortRecyclerViewAdapter.ViewHolder>(CategoryDiffCallback()) {

    fun moveItem(fromPosition: Int, toPosition: Int) {
        val current = currentList
        if (fromPosition !in current.indices || toPosition !in current.indices) return
        val updated = current.toMutableList()
        val item = updated.removeAt(fromPosition)
        updated.add(toPosition, item)
        submitList(updated)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: RvCategorySortItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.tvCategoryName.text = category
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvCategorySortItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
