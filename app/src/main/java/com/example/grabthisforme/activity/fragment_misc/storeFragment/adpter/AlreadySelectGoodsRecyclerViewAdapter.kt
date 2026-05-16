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
import com.example.grabthisforme.databinding.RvAlreadySelectItemBinding
import com.example.grabthisforme.model.goods.domain.Goods

class AlreadySelectGoodsRecyclerViewAdapter(
    private val onItemClick: (Goods) -> Unit,
    private val onMinusClick: (Goods) -> Unit,
    private val onPlusClick: (Goods) -> Unit
) : ListAdapter<Goods, AlreadySelectGoodsRecyclerViewAdapter.ViewHolder>(GoodsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick, onMinusClick, onPlusClick)
    }

    class ViewHolder(val binding: RvAlreadySelectItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val tagAdapter = StoreOwnerTagRecyclerViewAdapter()

        init {
            binding.rvTag.layoutManager =
                LinearLayoutManager(binding.root.context, RecyclerView.HORIZONTAL, false)
            binding.rvTag.adapter = tagAdapter
            binding.rvTag.itemAnimator = null
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvAlreadySelectItemBinding.inflate(inflater, parent, false)
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
            tagAdapter.submitList(goods.toTagList())
            binding.tvPriceSingle.text = String.format("￥%.2f", goods.price)
            binding.tvPriceDiscount.text = when {
                goods.discountPrice > 0 -> String.format("折扣价%.2f", goods.discountPrice)
                else -> ""
            }
            Glide.with(binding.root.context)
                .load(goods.pic)
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoods)
            binding.tvSaleNumber.text = goods.selectedCount.toString()
            binding.root.setOnClickListener { onItemClick(goods) }
            binding.btnMinus.setOnClickListener { onMinusClick(goods) }
            binding.btnPlus.setOnClickListener { onPlusClick(goods) }
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
            return oldItem.id == newItem.id &&
                oldItem.name == newItem.name &&
                oldItem.price == newItem.price &&
                oldItem.selectedCount == newItem.selectedCount &&
                oldItem.discountPrice == newItem.discountPrice &&
                oldItem.discountTag == newItem.discountTag &&
                oldItem.tag == newItem.tag
        }
    }
}
