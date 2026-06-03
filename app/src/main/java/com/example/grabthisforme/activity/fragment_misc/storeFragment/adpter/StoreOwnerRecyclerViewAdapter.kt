package com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.StoreOwnerGoodsItemUiModel
import com.example.grabthisforme.databinding.RvStoreOwnerGoodsItemBinding

class StoreOwnerRecyclerViewAdapter(
    private val onItemClick: (StoreOwnerGoodsItemUiModel) -> Unit
) : ListAdapter<StoreOwnerGoodsItemUiModel, StoreOwnerRecyclerViewAdapter.ViewHolder>(GoodsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(private val binding: RvStoreOwnerGoodsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tagAdapter = StoreOwnerTagRecyclerViewAdapter()

        init {
            binding.rvTag.layoutManager = LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
            binding.rvTag.adapter = tagAdapter
            binding.rvTag.itemAnimator = null
            binding.rvTag.setHasFixedSize(true)
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvStoreOwnerGoodsItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: StoreOwnerGoodsItemUiModel,
            onItemClick: (StoreOwnerGoodsItemUiModel) -> Unit
        ) {
            binding.tvTitle.text = item.title
            binding.tvPriceSingle.text = item.priceText
            binding.tvPriceDiscount.text = item.discountText
            binding.tvAdd.text = item.stockText
            tagAdapter.submitList(item.tags)

            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoods)

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class GoodsDiffCallback : DiffUtil.ItemCallback<StoreOwnerGoodsItemUiModel>() {
        override fun areItemsTheSame(oldItem: StoreOwnerGoodsItemUiModel, newItem: StoreOwnerGoodsItemUiModel): Boolean {
            return oldItem.goodsId == newItem.goodsId
        }

        override fun areContentsTheSame(oldItem: StoreOwnerGoodsItemUiModel, newItem: StoreOwnerGoodsItemUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
