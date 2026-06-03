package com.example.grabthisforme.activity.homeFragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvGoodsItemBinding
import com.example.grabthisforme.activity.homeFragment.ui_model.HomeStorePreviewItemUiModel

class RecyclerViewGoodsAdapter(
    private val clickListener: (goodsId: Long) -> Unit) : ListAdapter<HomeStorePreviewItemUiModel, RecyclerViewGoodsAdapter.ViewHolder>(
    GoodsDiffItemCallback()) {
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
        val goods = getItem(position)
        holder.bind(goods,clickListener)
    }

    class ViewHolder(val binding : RvGoodsItemBinding) : RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflate(parent : ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvGoodsItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
        fun bind(goods : HomeStorePreviewItemUiModel,clickListener: (Long) -> Unit){
            if (goods.isMoreEntry){
                binding.llTintIntoStore.visibility = View.VISIBLE
                binding.llGoods.visibility = View.GONE
            }else{
                binding.llTintIntoStore.visibility = View.GONE
                binding.llGoods.visibility = View.VISIBLE
            }
            binding.goodsPrice.text = goods.priceText
            binding.goodsMessage.text = goods.title
            Glide.with(binding.root.context)
                .load(goods.imageUrl)
                .error(R.drawable.food_pic)
                .placeholder(R.drawable.food_pic)
                .into(binding.ivGoodsPic)
            binding.root.setOnClickListener {
                goods.goodsId?.let(clickListener)
            }
        }

    }
    class GoodsDiffItemCallback: DiffUtil.ItemCallback<HomeStorePreviewItemUiModel>(){
        override fun areItemsTheSame(
            oldItem: HomeStorePreviewItemUiModel,
            newItem: HomeStorePreviewItemUiModel
        ): Boolean {
            return oldItem.goodsId == newItem.goodsId && oldItem.isMoreEntry == newItem.isMoreEntry
        }
        override fun areContentsTheSame(
            oldItem: HomeStorePreviewItemUiModel,
            newItem: HomeStorePreviewItemUiModel
        ): Boolean {
            return oldItem == newItem
        }

    }

}
