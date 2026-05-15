package com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvTagItemBinding

class StoreOwnerTagRecyclerViewAdapter :
    ListAdapter<String, StoreOwnerTagRecyclerViewAdapter.ViewHolder>(TagDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: RvTagItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvTagItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
        fun bind(tag: String) {
            binding.tvTag.text = tag
        }
    }

    class TagDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
