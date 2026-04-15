package com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material.FabPosition
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R


import com.example.grabthisforme.databinding.RvStoreSelectItemBinding

class StoreCategoryRecyclerViewAdapter(
    private val onItemClick: (String, Int) -> Unit
) : RecyclerView.Adapter<StoreCategoryRecyclerViewAdapter.ViewHolder>() {
    private var categoryList: List<String> = emptyList()
    private var selectedPosition = 0
    fun setCategoryList(list: List<String>) {
        categoryList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categoryList[position]
        holder.bind(item, position, selectedPosition == position, selectedPosition,onItemClick)
    }

    override fun getItemCount() = categoryList.size

    class ViewHolder(val binding: RvStoreSelectItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding =RvStoreSelectItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            category: String,
            position: Int,
            isSelected: Boolean,
            selectedPosition: Int,
            onItemClick: (String, Int) -> Unit
        ) {
            binding.tvCategory.text = category
            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.bg_rectangle_white)
            } else if (position == selectedPosition - 1){
                binding.root.setBackgroundResource(R.drawable.bg_rectangle_gray_rigtht_bottom_round)
            } else if (position == selectedPosition + 1){
                binding.root.setBackgroundResource(R.drawable.bg_rectangle_gray_right_top_round)
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_rectangle_gray)
            }

            binding.root.setOnClickListener {
                onItemClick(category, position)
            }
        }
    }

    fun updateSelectedPosition(position: Int) {
        if (selectedPosition == position) return
        val last = selectedPosition
        selectedPosition = position
        notifyItemChanged(last)
        notifyItemChanged(selectedPosition)
        if (last > 0) notifyItemChanged(last - 1)
        if (last < categoryList.size - 1) notifyItemChanged(last + 1)
        notifyItemChanged(selectedPosition)
        if (selectedPosition > 0) notifyItemChanged(selectedPosition - 1)
        if (selectedPosition < categoryList.size - 1) notifyItemChanged(selectedPosition + 1)
    }
}