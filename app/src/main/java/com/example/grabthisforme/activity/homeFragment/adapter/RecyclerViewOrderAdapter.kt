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

            binding.goodsMessage.text = "要求：${order.goods.message}"
            binding.goodsPrice.text = "商品价格：￥${order.goods.price}"
            binding.shelfNumber.text = "货架号：${order.shelf_number}"
            binding.aimPosition.text = "目的地：${order.aim_position}"
            binding.goodsName.text = "商品：${order.goods.name}"
            binding.sendTime.text = "配送时间：${formatTime(order.startTime)} - ${formatTime(order.endTime)}"
            binding.timeLeft.text = formatTimeLeft(order.startTime, order.endTime)

            val photoUrl = order.goods.pic
            if (photoUrl.isNotEmpty()) {
                binding.goodsImg.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_back_charactor2)
                    .error(R.drawable.ic_back_charactor2)
                    .into(binding.goodsImg)
            } else {
                binding.goodsImg.visibility = View.GONE
            }

            Glide.with(binding.root.context)
                .load(order.buyer.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)

            if (userId != null || order.isBuyerSelf) {
                binding.ivState.visibility = View.VISIBLE
                if (isBuyerSelf) {
                    binding.llOrderItem.setBackgroundResource(R.drawable.bg_arc_gradient)
                    binding.ivState.setImageResource(R.drawable.ic_wait_receive)
                } else {
                    binding.llOrderItem.setBackgroundResource(R.drawable.bg_arc_gradient_green)
                    binding.ivState.setImageResource(R.drawable.ic_wait_send)
                }
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
            return currentTime <= endTime
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
