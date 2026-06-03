package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.ui_model.SecondhandGoodsCardUiModel
import com.example.grabthisforme.databinding.RvSecondhandGoodsItemBinding

class SecondhandGoodsRecyclerViewAdapter(
    private val clickListener: (goodsId: Long) -> Unit
) : ListAdapter<SecondhandGoodsCardUiModel, SecondhandGoodsRecyclerViewAdapter.ViewHolder>(
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

        fun bind(item: SecondhandGoodsCardUiModel, clickListener: (Long) -> Unit) {
            binding.tvGoodsName.text = item.goodsName
            binding.tvGoodsPrice.text = item.priceText
            binding.tvGoodsOriginalPrice.text = item.originalPriceText
            binding.tvGoodsQuality.text = item.qualityText
            binding.tvGoodsMessage.text = item.messageText
            binding.tvGoodsTradeHint.text = item.tradeHintText
            binding.tvGoodsUsedTime.text = item.usedTimeText
            binding.tvGoodsSaleCount.text = item.saleCountText

            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoodsPic)

            itemView.setOnClickListener {
                clickListener(item.goodsId)
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

    class SecondhandGoodsDiffItemCallback : DiffUtil.ItemCallback<SecondhandGoodsCardUiModel>() {
        override fun areItemsTheSame(oldItem: SecondhandGoodsCardUiModel, newItem: SecondhandGoodsCardUiModel): Boolean {
            return oldItem.goodsId == newItem.goodsId
        }

        override fun areContentsTheSame(oldItem: SecondhandGoodsCardUiModel, newItem: SecondhandGoodsCardUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
