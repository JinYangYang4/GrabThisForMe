package com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
        val goods = getItem(position)
        holder.bind(goods, onAddClick, onItemClick)
    }

    class ViewHolder(val binding: RvStoreGoodsItemBinding) : RecyclerView.ViewHolder(binding.root) {

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
            binding.tvTag.text = goods.tag.ifEmpty { "秒送" }
            binding.tvPriceSingle.text = String.format("¥%.2f", goods.price)
            binding.tvPriceDiscount.text = when {
                goods.discountTag.isNotEmpty() -> goods.discountTag
                goods.discountPrice > 0 -> String.format("优惠价 ¥%.2f", goods.discountPrice)
                else -> ""
            }
            binding.root.setOnClickListener { onItemClick(goods) }
            binding.ivAdd.setOnClickListener { onAddClick(goods) }
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
