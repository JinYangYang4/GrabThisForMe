package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.example.grabthisforme.databinding.RvSecondGoodsItemBinding
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods

class SecondhandGoodsRecyclerViewAdapter(
    private val clickListener: (goodsId: Long) -> Unit
) : ListAdapter<SecondhandGoods, SecondhandGoodsRecyclerViewAdapter.ViewHolder>(
    SecondhandGoodsDiffItemCallback()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val secondhandGoods = getItem(position)
        holder.bind(secondhandGoods, clickListener)
    }

    class ViewHolder(val binding: RvSecondGoodsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvSecondGoodsItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(secondhandGoods: SecondhandGoods, clickListener: (Long) -> Unit) {
            binding.goodsPrice.text = "¥${secondhandGoods.price}"
            binding.goodsMessage.text = secondhandGoods.name
            itemView.setOnClickListener {
                clickListener(secondhandGoods.id)
            }
        }
    }
    class SecondhandGoodsDiffItemCallback : DiffUtil.ItemCallback<SecondhandGoods>() {
        override fun areItemsTheSame(
            oldItem: SecondhandGoods,
            newItem: SecondhandGoods
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SecondhandGoods,
            newItem: SecondhandGoods
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.name == newItem.name &&
                    oldItem.price == newItem.price &&
                    oldItem.sale_number == newItem.sale_number &&
                    oldItem.originalPrice == newItem.originalPrice &&
                    oldItem.quality == newItem.quality
        }
    }
}
