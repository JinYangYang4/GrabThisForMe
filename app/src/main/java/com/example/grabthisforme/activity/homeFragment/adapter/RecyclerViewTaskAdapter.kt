package com.example.grabthisforme.activity.homeFragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.homeFragment.ui_model.OrderListItemUiModel
import com.example.grabthisforme.databinding.TaskRvItemBinding

class RecyclerViewTaskAdapter(
    private val clickListener: (orderId: String) -> Unit
) : ListAdapter<OrderListItemUiModel, RecyclerViewTaskAdapter.ViewHolder>(ViewHolder.TaskDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder(val binding: TaskRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskRvItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(item: OrderListItemUiModel, clickListener: (String) -> Unit) {
            binding.sendTime.text = item.sendTimeText
            binding.timeLeft.text = item.timeLeftText
            binding.goodsMessage.text = item.goodsMessage
            binding.goodsName.text = item.goodsName
            binding.goodsPrice.text = item.goodsPriceText
            binding.shelfNumber.text = item.shelfNumberText
            binding.aimPosition.text = item.aimPositionText

            if (!item.goodsImageUrl.isNullOrBlank()) {
                binding.goodsImg.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(item.goodsImageUrl)
                    .placeholder(R.drawable.food_pic)
                    .error(R.drawable.food_pic)
                    .into(binding.goodsImg)
            } else {
                binding.goodsImg.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(item.buyerAvatarUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)

            binding.ivState.visibility = View.VISIBLE
            binding.llTaskItem.setBackgroundResource(R.drawable.bg_create_goods_card)
            if (item.isBuyerSelf) {
                binding.ivState.setImageResource(R.drawable.ic_wait_receive)
            } else {
                binding.ivState.setImageResource(R.drawable.ic_wait_send)
            }

            itemView.setOnClickListener {
                clickListener.invoke(item.orderId)
            }
            itemView.alpha = 0f
            itemView.translationY = 18f
            itemView.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        }

        class TaskDiffItemCallback : DiffUtil.ItemCallback<OrderListItemUiModel>() {
            override fun areItemsTheSame(oldItem: OrderListItemUiModel, newItem: OrderListItemUiModel): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: OrderListItemUiModel, newItem: OrderListItemUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
