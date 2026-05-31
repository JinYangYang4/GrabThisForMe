package com.example.grabthisforme.activity.homeFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvStoreItemBinding
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.domain.Store

class RecyclerViewStoreAdapter(
    private var onStoreClickListener: ((Store) -> Unit)? = null,
) : ListAdapter<Store, RecyclerViewStoreAdapter.StoreViewHolder>(StoreDiffCallback()) {

    inner class StoreViewHolder(
        private val binding: RvStoreItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var goodsAdapter: RecyclerViewGoodsAdapter

        init {
            binding.rvGoods.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                isNestedScrollingEnabled = false
                setHasFixedSize(true)
            }
        }

        fun bind(store: Store) {
            binding.tvStore.text = store.name.ifBlank { "校园店铺" }
            binding.tvSaleNumber.text = "已售 ${store.salesVolume}"
            binding.tvDistance.text = "约 1km"

            goodsAdapter = RecyclerViewGoodsAdapter {}
            binding.rvGoods.adapter = goodsAdapter

            var sourceGoods = if (store.goodsAll.isNotEmpty()) store.goodsAll else Goods.get20RepeatGoods()
            if (sourceGoods.size > 15) {
                sourceGoods = sourceGoods.take(14)
            }
            val goodsMutableList = sourceGoods.toMutableList()
            goodsMutableList.add(Goods(-1L))
            goodsAdapter.submitList(goodsMutableList)

            itemView.setOnClickListener {
                onStoreClickListener?.invoke(store)
            }
            itemView.alpha = 0f
            itemView.translationY = 18f
            itemView.animate().alpha(1f).translationY(0f).setDuration(260L).start()

            Glide.with(binding.root.context)
                .load(store.pic)
                .error(R.drawable.ic_store)
                .placeholder(R.drawable.ic_store)
                .into(binding.ivStore)
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
