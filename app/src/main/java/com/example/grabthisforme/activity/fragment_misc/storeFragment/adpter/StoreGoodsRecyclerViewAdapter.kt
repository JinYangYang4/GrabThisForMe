package com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreOwnerTagRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.StoreGoodsListItemUiModel
import com.example.grabthisforme.databinding.RvStoreGoodsItemBinding

class StoreGoodsRecyclerViewAdapter(
    private val onAddClick: (StoreGoodsListItemUiModel) -> Unit,
    private val onItemClick: (StoreGoodsListItemUiModel) -> Unit
) : ListAdapter<StoreGoodsListItemUiModel, StoreGoodsRecyclerViewAdapter.ViewHolder>(GoodsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onAddClick, onItemClick)
    }

    class ViewHolder(val binding: RvStoreGoodsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tagAdapter = StoreOwnerTagRecyclerViewAdapter()

        init {
            binding.rvTag.layoutManager =
                LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
            binding.rvTag.adapter = tagAdapter
            binding.rvTag.itemAnimator = null
            binding.rvTag.setHasFixedSize(true)
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvStoreGoodsItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: StoreGoodsListItemUiModel,
            onAddClick: (StoreGoodsListItemUiModel) -> Unit,
            onItemClick: (StoreGoodsListItemUiModel) -> Unit
        ) {
            binding.tvTitle.text = item.title
            tagAdapter.submitList(item.tags)
            binding.tvPriceSingle.text = item.priceText
            binding.tvPriceDiscount.text = item.discountText
            Glide.with(binding.root.context)
                .load(item.imageUrl)
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoods)
            binding.root.setOnClickListener { onItemClick(item) }
            binding.ivAdd.setOnClickListener { onAddClick(item) }
        }
    }

    class GoodsDiffCallback : DiffUtil.ItemCallback<StoreGoodsListItemUiModel>() {
        override fun areItemsTheSame(oldItem: StoreGoodsListItemUiModel, newItem: StoreGoodsListItemUiModel): Boolean {
            return oldItem.goodsId == newItem.goodsId
        }

        override fun areContentsTheSame(oldItem: StoreGoodsListItemUiModel, newItem: StoreGoodsListItemUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
