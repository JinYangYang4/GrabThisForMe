package com.example.grabthisforme.activity.fragment_misc.my_store.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.my_store.ui_model.MyStoreListItemUiModel
import com.example.grabthisforme.databinding.RvItemMyStoreBinding

class MyStoreRecyclerViewAdapter(
    private val clickListener: (MyStoreListItemUiModel) -> Unit
) : ListAdapter<MyStoreListItemUiModel, MyStoreRecyclerViewAdapter.ViewHolder>(
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

        fun bind(item: MyStoreListItemUiModel, clickListener: (MyStoreListItemUiModel) -> Unit) {
            binding.tvShopName.text = item.storeName
            binding.tvShopTime.text = item.businessHoursText
            binding.tvShopAddress.text = item.addressText
            binding.tvShopProfit.text = item.salesText

            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(R.drawable.ic_store)
                .error(R.drawable.ic_store)
                .into(binding.ivShopAvatar)

            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }

        class StoreDiffItemCallback : DiffUtil.ItemCallback<MyStoreListItemUiModel>() {
            override fun areItemsTheSame(oldItem: MyStoreListItemUiModel, newItem: MyStoreListItemUiModel): Boolean {
                return oldItem.storeId == newItem.storeId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: MyStoreListItemUiModel, newItem: MyStoreListItemUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
