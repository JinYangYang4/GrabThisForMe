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
import com.example.grabthisforme.model.goods.domain.Goods

class RecyclerViewGoodsAdapter(
    private val clickListener: (goodsId: Long) -> Unit) : ListAdapter<Goods, RecyclerViewGoodsAdapter.ViewHolder>(
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
        fun bind(goods : Goods,clickListener: (Long) -> Unit){
            if (goods.id == -1L){
                binding.llTintIntoStore.visibility = View.VISIBLE
                binding.llGoods.visibility = View.GONE
            }else{
                binding.llTintIntoStore.visibility = View.GONE
                binding.llGoods.visibility = View.VISIBLE
            }
            binding.goodsPrice.text = goods.price.toString()
            binding.goodsMessage.text = goods.name
            Glide.with(binding.root.context)
                .load(goods.pic)
                .error(R.drawable.food_pic)
                .placeholder(R.drawable.food_pic)
                .into(binding.ivGoodsPic)
        }

    }
    class GoodsDiffItemCallback: DiffUtil.ItemCallback<Goods>(){
        override fun areItemsTheSame(
            oldItem: Goods,
            newItem: Goods
        ): Boolean {
            return oldItem == newItem
        }
        override fun areContentsTheSame(
            oldItem: Goods,
            newItem: Goods
        ): Boolean {
            return oldItem.id  == newItem.id
        }

    }

}

