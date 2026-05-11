package com.example.grabthisforme.activity.fragment_misc.my_store.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvItemMyStoreBinding
import com.example.grabthisforme.model.store.domain.Store

class MyStoreRecyclerViewAdapter(
    private val clickListener: (Store) -> Unit
) : ListAdapter<Store, MyStoreRecyclerViewAdapter.ViewHolder>(
    ViewHolder.StoreDiffItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder(private val binding: RvItemMyStoreBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvItemMyStoreBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(store: Store, clickListener: (Store) -> Unit) {
            binding.tvShopName.text = store.name
            binding.tvShopTime.text = store.businessHours?.takeIf { it.isNotBlank() }?.let {
                "营业时间：$it"
            } ?: "营业时间：暂无"
            binding.tvShopAddress.text = store.address
            binding.tvShopProfit.text = "销量：${store.salesVolume}"

            Glide.with(binding.root)
                .load(store.pic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivShopAvatar)

            binding.root.setOnClickListener {
                clickListener.invoke(store)
            }
        }

        class StoreDiffItemCallback : DiffUtil.ItemCallback<Store>() {
            override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean {
                return oldItem.id == newItem.id
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean {
                return oldItem == newItem
            }
        }
    }
}
