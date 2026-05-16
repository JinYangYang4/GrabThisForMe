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
import com.example.grabthisforme.databinding.RvStoreGoodsItemBinding
import com.example.grabthisforme.model.goods.domain.Goods

class StoreGoodsRecyclerViewAdapter(
    private val onAddClick: (Goods) -> Unit,
    private val onItemClick: (Goods) -> Unit
) : ListAdapter<Goods, StoreGoodsRecyclerViewAdapter.ViewHolder>(GoodsDiffCallback()) {

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
            goods: Goods,
            onAddClick: (Goods) -> Unit,
            onItemClick: (Goods) -> Unit
        ) {
            binding.tvTitle.text = goods.name
            tagAdapter.submitList(goods.toTagList())
            binding.tvPriceSingle.text = String.format("楼%.2f", goods.price)
            binding.tvPriceDiscount.text = when {
                goods.discountTag.isNotEmpty() -> goods.discountTag
                goods.discountPrice > 0 -> String.format("浼樻儬浠?楼%.2f", goods.discountPrice)
                else -> ""
            }
            Glide.with(binding.root.context)
                .load(goods.pic)
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoods)
            binding.root.setOnClickListener { onItemClick(goods) }
            binding.ivAdd.setOnClickListener { onAddClick(goods) }
        }

        private fun Goods.toTagList(): List<String> {
            val tags = mutableListOf<String>()
            if (discountTag.isNotBlank()) {
                tags.add(discountTag)
            }
            if (tag.isNotBlank()) {
                tags.addAll(tag.split(Regex("[,/;|\\s]+")).filter { it.isNotBlank() })
            }
            if (tags.isEmpty()) {
                tags.add("默认")
            }
            return tags.distinct()
        }
    }

    class GoodsDiffCallback : DiffUtil.ItemCallback<Goods>() {
        override fun areItemsTheSame(oldItem: Goods, newItem: Goods): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Goods, newItem: Goods): Boolean {
            return oldItem.id == newItem.id
        }
    }
}
