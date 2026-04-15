package com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvAlreadySelectItemBinding


import com.example.grabthisforme.model.goods.Goods

class AlreadySelectGoodsRecyclerViewAdapter(
    private val onItemClick: (Goods) -> Unit,
    private val onMinusClick: (Goods) -> Unit,
    private val onPlusClick: (Goods) -> Unit
) : ListAdapter<Goods, AlreadySelectGoodsRecyclerViewAdapter.ViewHolder>(GoodsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val goods = getItem(position)
        holder.bind(goods, onItemClick, onMinusClick, onPlusClick)
    }

    class ViewHolder(val binding: RvAlreadySelectItemBinding) : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding =RvAlreadySelectItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            goods: Goods,
            onItemClick: (Goods) -> Unit,
            onMinusClick: (Goods) -> Unit,
            onPlusClick: (Goods) -> Unit
        ) {
            binding.tvTitle.text = goods.name
            binding.tvTag.text = goods.tag.ifEmpty { "秒送" }
            binding.tvPriceSingle.text = String.format("¥%.2f", goods.price)
            binding.tvPriceDiscount.text = when {
                goods.discountTag.isNotEmpty() -> goods.discountTag
                goods.discountPrice > 0 -> String.format("买3件¥%.2f/件", goods.discountPrice)
                else -> ""
            }

            binding.tvSaleNumber.text = goods.selectedCount.toString()
            binding.root.setOnClickListener {
                onItemClick(goods)
                goods.discountPrice
            }

            binding.btnMinus.setOnClickListener {
                onMinusClick(goods)
            }

            binding.btnPlus.setOnClickListener {
                onPlusClick(goods)
            }
        }
    }

    class GoodsDiffCallback : DiffUtil.ItemCallback<Goods>() {
        override fun areItemsTheSame(oldItem: Goods, newItem: Goods): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Goods, newItem: Goods): Boolean {
            // 对比所有会变的字段，保证UI自动刷新
            return oldItem.id == newItem.id
                    && oldItem.name == newItem.name
                    && oldItem.price == newItem.price
                    && oldItem.selectedCount == newItem.selectedCount
        }
    }
}