package com.example.grabthisforme.activity.homeFragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvOrderItemBinding
import com.example.grabthisforme.model.order.domain.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerViewOrderAdapter(
    var userId: Long? = null,
    private val clickListener: (taskId: Long) -> Unit
) : ListAdapter<Order, RecyclerViewOrderAdapter.ViewHolder>(ViewHolder.OrderDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, userId)
    }

    class ViewHolder(private val binding: RvOrderItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvOrderItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(order: Order, clickListener: (Long) -> Unit, userId: Long?) {
            val isBuyerSelf = order.isBuyerSelf || (userId != null && order.buyer.id == userId)

            binding.goodsMessage.text = order.goods.message.ifBlank { "暂无补充要求" }
            binding.goodsPrice.text = "商品价 ¥${order.goods.price}"
            binding.shelfNumber.text = "货架号 ${order.shelf_number.ifBlank { "未填写" }}"
            binding.aimPosition.text = "送达 ${order.aim_position.ifBlank { "待确认" }}"
            binding.goodsName.text = order.goods.name.ifBlank { "待采购商品" }
            binding.sendTime.text = "配送时间 ${formatTime(order.startTime)} - ${formatTime(order.endTime)}"
            binding.timeLeft.text = formatTimeLeft(order.startTime, order.endTime)

            val photoUrl = order.goods.pic
            if (photoUrl.isNotEmpty()) {
                binding.goodsImg.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(photoUrl)
                    .placeholder(R.drawable.food_pic)
                    .error(R.drawable.food_pic)
                    .into(binding.goodsImg)
            } else {
                binding.goodsImg.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(order.buyer.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)

            binding.llOrderItem.setBackgroundResource(R.drawable.bg_create_goods_card)
            if (userId != null || order.isBuyerSelf) {
                binding.ivState.visibility = View.VISIBLE
                binding.ivState.setImageResource(
                    if (isBuyerSelf) R.drawable.ic_wait_receive else R.drawable.ic_wait_send
                )
                if (checkOrderStatus(order.endTime)) {
                    binding.ivState.setImageResource(R.drawable.ic_already_over)
                }
            } else {
                binding.ivState.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val taskId = order.orderId.toLongOrNull() ?: 0L
                clickListener.invoke(taskId)
            }
            itemView.isClickable = true
            itemView.isFocusable = true
            itemView.alpha = 0f
            itemView.translationY = 18f
            itemView.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        }

        private fun formatTime(timeStamp: Long): String {
            return try {
                val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(timeStamp))
            } catch (e: Exception) {
                "时间异常"
            }
        }

        private fun checkOrderStatus(endTime: Long): Boolean {
            val currentTime = System.currentTimeMillis()
            return currentTime > endTime
        }

        private fun formatTimeLeft(startTime: Long, endTime: Long): String {
            val duration = endTime - startTime
            if (duration <= 0) return "已送达"

            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)

            return when {
                hours > 0 && minutes > 0 -> "剩余 ${hours} 小时 ${minutes} 分钟"
                hours > 0 -> "剩余 ${hours} 小时"
                else -> "剩余 ${minutes} 分钟"
            }
        }

        class OrderDiffItemCallback : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem == newItem
            }
        }
    }
}
