package com.example.grabthisforme.activity.fragment_misc.goodsFragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.ui_model.GoodsMarketplaceItemUiModel
import com.example.grabthisforme.databinding.RvMarketplaceGoodsItemBinding

class GoodsMarketplaceRecyclerViewAdapter(
    private val clickListener: (GoodsMarketplaceItemUiModel) -> Unit
) : ListAdapter<GoodsMarketplaceItemUiModel, GoodsMarketplaceRecyclerViewAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder(private val binding: RvMarketplaceGoodsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GoodsMarketplaceItemUiModel, clickListener: (GoodsMarketplaceItemUiModel) -> Unit) {
            binding.tvGoodsName.text = item.goodsName
            binding.tvGoodsMessage.text = item.goodsMessage
            binding.tvGoodsPrice.text = item.priceText

            binding.tvGoodsDiscountPrice.visibility = if (item.showDiscountPrice) View.VISIBLE else View.GONE
            binding.tvGoodsDiscountPrice.text = item.discountPriceText

            binding.tvGoodsDiscountTag.visibility = if (item.showDiscountTag) View.VISIBLE else View.GONE
            binding.tvGoodsDiscountTag.text = item.discountTag

            binding.tvGoodsStoreType.text = item.storeTypeText
            binding.tvGoodsHeat.text = item.heatText
            binding.tvGoodsHeat.setBackgroundResource(
                if (item.isHot) R.drawable.bg_chip_warm else R.drawable.bg_chip_mint
            )
            binding.tvGoodsStatus.text = item.statusText

            Glide.with(binding.root)
                .load(item.imageUrl)
                .placeholder(R.drawable.food_pic)
                .error(R.drawable.food_pic)
                .into(binding.ivGoodsPic)

            itemView.setOnClickListener { clickListener(item) }
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvMarketplaceGoodsItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GoodsMarketplaceItemUiModel>() {
        override fun areItemsTheSame(oldItem: GoodsMarketplaceItemUiModel, newItem: GoodsMarketplaceItemUiModel): Boolean {
            return oldItem.goodsId == newItem.goodsId
        }

        override fun areContentsTheSame(oldItem: GoodsMarketplaceItemUiModel, newItem: GoodsMarketplaceItemUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
