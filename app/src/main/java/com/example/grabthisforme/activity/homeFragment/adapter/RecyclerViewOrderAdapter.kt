package com.example.grabthisforme.activity.homeFragment.adapter

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.homeFragment.ui_model.OrderListItemUiModel
import com.example.grabthisforme.databinding.RvOrderItemBinding

class RecyclerViewOrderAdapter(
    private val clickListener: (orderId: String) -> Unit
) : ListAdapter<OrderListItemUiModel, RecyclerViewOrderAdapter.ViewHolder>(ViewHolder.OrderDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, position)
    }

    class ViewHolder(private val binding: RvOrderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvOrderItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(item: OrderListItemUiModel, clickListener: (String) -> Unit, position: Int) {
            binding.goodsMessage.text = item.goodsMessage
            binding.goodsPrice.text = item.goodsPriceText
            binding.shelfNumber.text = item.shelfNumberText
            binding.aimPosition.text = item.aimPositionText
            binding.goodsName.text = item.goodsName
            binding.sendTime.text = item.sendTimeText
            binding.timeLeft.text = item.timeLeftText

            if (!item.goodsImageUrl.isNullOrBlank()) {
                binding.goodsImg.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(item.goodsImageUrl)
                    .placeholder(R.drawable.food_pic)
                    .error(R.drawable.food_pic)
                    .into(binding.goodsImg)
            } else {
                binding.goodsImg.setImageResource(R.drawable.food_pic)
                binding.goodsImg.visibility = View.VISIBLE
            }

            Glide.with(binding.root.context)
                .load(item.buyerAvatarUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)

            if (!item.statusBadgeText.isNullOrBlank()) {
                binding.tvStateBadge.visibility = View.VISIBLE
                binding.tvStateBadge.text = item.statusBadgeText
            } else {
                binding.tvStateBadge.visibility = View.GONE
            }

            itemView.setOnClickListener {
                clickListener.invoke(item.orderId)
            }
            itemView.isClickable = true
            itemView.isFocusable = true

            itemView.alpha = 0f
            itemView.translationY = 18f
            itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(260L)
                .setStartDelay((position * 30).toLong())
                .start()
        }

        class OrderDiffItemCallback : DiffUtil.ItemCallback<OrderListItemUiModel>() {
            override fun areItemsTheSame(oldItem: OrderListItemUiModel, newItem: OrderListItemUiModel): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(oldItem: OrderListItemUiModel, newItem: OrderListItemUiModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    class OrderItemDecoration(private val spacingPx: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.left = spacingPx
            outRect.right = spacingPx
            outRect.top = spacingPx / 2
            outRect.bottom = spacingPx / 2
        }
    }
}
