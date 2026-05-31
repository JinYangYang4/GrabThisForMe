package com.example.grabthisforme.activity.mainactivity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvRecentlyUserItemBinding
import com.example.grabthisforme.model.store.domain.Store


class RVRecentStoreAdapter(val clickListener : (StoreId : Long) -> Unit) : ListAdapter<Store, RVRecentStoreAdapter.ViewHolder>(
    DiffItemCallback()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val store = getItem(position)
        holder.bind(store,clickListener)
    }

    class ViewHolder(val binding: RvRecentlyUserItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflate(parent : ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvRecentlyUserItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
        fun bind(store : Store,clickListener: (Long) -> Unit){
            binding.tvName.text = store.name
            binding.tvBadge.text = store.type.ifBlank { "店铺" }
            Glide.with(binding.ivHeadPic)
                .load(store.pic)
                .placeholder(R.drawable.ic_store)
                .error(R.drawable.ic_store)
                .into(binding.ivHeadPic)
            binding.root.setOnClickListener {
                clickListener(store.id)
            }
        }
    }
    class DiffItemCallback : DiffUtil.ItemCallback<Store>() {
        override fun areContentsTheSame(
            oldItem: Store,
            newItem: Store
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: Store,
            newItem: Store
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
