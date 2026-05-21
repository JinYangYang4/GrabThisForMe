package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvSecondhandGoodsItemBinding
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods

class SecondhandGoodsRecyclerViewAdapter(
    private val clickListener: (goodsId: Long) -> Unit
) : ListAdapter<SecondhandGoods, SecondhandGoodsRecyclerViewAdapter.ViewHolder>(
    SecondhandGoodsDiffItemCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder(private val binding: RvSecondhandGoodsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(secondhandGoods: SecondhandGoods, clickListener: (Long) -> Unit) {
            binding.tvGoodsName.text = secondhandGoods.name
            binding.tvGoodsPrice.text = "¥${secondhandGoods.price}"
            binding.tvGoodsOriginalPrice.text = "¥${secondhandGoods.originalPrice}"
            binding.tvGoodsQuality.text = secondhandGoods.quality
            binding.tvGoodsSaleCount.text = "已售 ${secondhandGoods.soldCount}"

            Glide.with(binding.root)
                .load(secondhandGoods.pic.takeIf { it.isNotBlank() })
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoodsPic)

            itemView.setOnClickListener {
                clickListener(secondhandGoods.id)
            }
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvSecondhandGoodsItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class SecondhandGoodsDiffItemCallback : DiffUtil.ItemCallback<SecondhandGoods>() {
        override fun areItemsTheSame(oldItem: SecondhandGoods, newItem: SecondhandGoods): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SecondhandGoods, newItem: SecondhandGoods): Boolean {
            return oldItem.name == newItem.name &&
                oldItem.price == newItem.price &&
                oldItem.sale_number == newItem.sale_number &&
                oldItem.originalPrice == newItem.originalPrice &&
                oldItem.quality == newItem.quality &&
                oldItem.pic == newItem.pic &&
                oldItem.soldCount == newItem.soldCount
        }
    }
}
