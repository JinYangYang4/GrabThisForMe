package com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvCategoryManageItemBinding

class CategoryManageRecyclerViewAdapter(
    private val actionText: String,
    private val onItemClick: (String, Int) -> Unit
) : ListAdapter<String, CategoryManageRecyclerViewAdapter.ViewHolder>(CategoryDiffCallback()) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun submitList(list: List<String>?) {
        super.submitList(list?.toList())
        val size = list?.size ?: 0
        if (selectedPosition >= size) selectedPosition = RecyclerView.NO_POSITION
    }

    fun updateSelectedPosition(position: Int) {
        if (selectedPosition == position) return
        val previous = selectedPosition
        selectedPosition = position
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous)
        }
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(
            category = category,
            actionText = actionText,
            isSelected = selectedPosition == position,
            onItemClick = { onItemClick(category, position) }
        )
    }

    class ViewHolder(private val binding: RvCategoryManageItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            category: String,
            actionText: String,
            isSelected: Boolean,
            onItemClick: () -> Unit
        ) {
            binding.tvCategoryName.text = category
            binding.tvAction.text = actionText
            binding.root.setBackgroundResource(
                if (isSelected) R.drawable.bg_rounded_white else android.R.color.transparent
            )
            binding.root.setOnClickListener { onItemClick() }
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvCategoryManageItemBinding.inflate(inflater, parent, false)
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
