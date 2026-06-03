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
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.SelectedGoodsItemUiModel
import com.example.grabthisforme.databinding.RvAlreadySelectItemBinding

class AlreadySelectGoodsRecyclerViewAdapter(
    private val onItemClick: (SelectedGoodsItemUiModel) -> Unit,
    private val onMinusClick: (SelectedGoodsItemUiModel) -> Unit,
    private val onPlusClick: (SelectedGoodsItemUiModel) -> Unit
) : ListAdapter<SelectedGoodsItemUiModel, AlreadySelectGoodsRecyclerViewAdapter.ViewHolder>(
    GoodsDiffCallback()
) {

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
            binding.rvTag.setHasFixedSize(true)
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvAlreadySelectItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            item: SelectedGoodsItemUiModel,
            onItemClick: (SelectedGoodsItemUiModel) -> Unit,
            onMinusClick: (SelectedGoodsItemUiModel) -> Unit,
            onPlusClick: (SelectedGoodsItemUiModel) -> Unit
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
            binding.tvSaleNumber.text = item.selectedCount.toString()
            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnMinus.setOnClickListener { onMinusClick(item) }
            binding.btnPlus.setOnClickListener { onPlusClick(item) }
        }
    }

    class GoodsDiffCallback : DiffUtil.ItemCallback<SelectedGoodsItemUiModel>() {
        override fun areItemsTheSame(
            oldItem: SelectedGoodsItemUiModel,
            newItem: SelectedGoodsItemUiModel
        ): Boolean {
            return oldItem.goodsId == newItem.goodsId
        }

        override fun areContentsTheSame(
            oldItem: SelectedGoodsItemUiModel,
            newItem: SelectedGoodsItemUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}
