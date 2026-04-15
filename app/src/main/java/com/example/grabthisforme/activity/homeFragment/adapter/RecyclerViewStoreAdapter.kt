package com.example.grabthisforme.activity.homeFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvStoreItemBinding
import com.example.grabthisforme.model.goods.Goods
import com.example.grabthisforme.model.store.Store


class RecyclerViewStoreAdapter(private var onStoreClickListener: ((Store) -> Unit)? = null) : ListAdapter<Store, RecyclerViewStoreAdapter.StoreViewHolder>(StoreDiffCallback()) {
    inner class StoreViewHolder(private val binding: RvStoreItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var goodsAdapter : RecyclerViewGoodsAdapter

        init {
            binding.rvGoods.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL,false)
                isNestedScrollingEnabled = false
                setHasFixedSize(true)
            }
        }

        fun bind(store: Store) {
            binding.tvStore.text = store.name
            binding.tvSaleNumber.text = "销售量：" + store.salesVolume.toString()
            binding.tvDistance.text = "距离：1km"
            goodsAdapter = RecyclerViewGoodsAdapter(){}
            binding.rvGoods.adapter = goodsAdapter
            val goodsMutableList = Goods.get20RepeatGoods().toMutableList()
            goodsMutableList.add(Goods(-1L))
            val goodsList: List<Goods> = goodsMutableList
            goodsAdapter.submitList( goodsList)

            itemView.setOnClickListener {
                onStoreClickListener?.invoke(store)
            }
        }
    }
    class StoreDiffCallback : DiffUtil.ItemCallback<Store>() {
        override fun areItemsTheSame(oldItem: Store, newItem: Store): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Store, newItem: Store): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = RvStoreItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}