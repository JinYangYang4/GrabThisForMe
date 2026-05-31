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
        holder.bind(getItem(position), clickListener, userId, position)
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

        fun bind(order: Order, clickListener: (Long) -> Unit, userId: Long?, position: Int) {
            val isBuyerSelf = order.isBuyerSelf || (userId != null && order.buyer.id == userId)

            binding.goodsMessage.text = order.goods.message.ifBlank { "\u6682\u65e0\u8865\u5145\u8981\u6c42" }
            binding.goodsPrice.text = "\u5546\u54c1\u4ef7 \u00a5${order.goods.price}"
            binding.shelfNumber.text = "\u8d27\u67b6\uff1a${order.shelf_number.ifBlank { "\u672a\u586b\u5199" }}"
            binding.aimPosition.text = "\u9001\u8fbe\uff1a${order.aim_position.ifBlank { "\u5f85\u786e\u8ba4" }}"
            binding.goodsName.text = order.goods.name.ifBlank { "\u5f85\u91c7\u8d2d\u5546\u54c1" }
            binding.sendTime.text = "\u914d\u9001\uff1a${formatTime(order.startTime)} - ${formatTime(order.endTime)}"
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
                binding.goodsImg.setImageResource(R.drawable.food_pic)
                binding.goodsImg.visibility = View.VISIBLE
            }

            Glide.with(binding.root.context)
                .load(order.buyer.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)

            if (userId != null || order.isBuyerSelf) {
                binding.tvStateBadge.visibility = View.VISIBLE
                binding.tvStateBadge.text = when {
                    checkOrderStatus(order.endTime) -> "\u5df2\u5b8c\u6210"
                    isBuyerSelf -> "\u5f85\u6536\u8d27"
                    else -> "\u5f85\u9001\u8d27"
                }
            } else {
                binding.tvStateBadge.visibility = View.GONE
            }

            itemView.setOnClickListener {
                val taskId = order.orderId.toLongOrNull() ?: 0L
                clickListener.invoke(taskId)
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

        private fun formatTime(timeStamp: Long): String {
            return try {
                val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(timeStamp))
            } catch (e: Exception) {
                "\u65f6\u95f4\u5f02\u5e38"
            }
        }

        private fun checkOrderStatus(endTime: Long): Boolean {
            return System.currentTimeMillis() > endTime
        }

        private fun formatTimeLeft(startTime: Long, endTime: Long): String {
            val duration = endTime - startTime
            if (duration <= 0) return "\u5df2\u9001\u8fbe"

            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)

            return when {
                hours > 0 && minutes > 0 -> "\u5269\u4f59 ${hours} \u5c0f\u65f6 ${minutes} \u5206\u949f"
                hours > 0 -> "\u5269\u4f59 ${hours} \u5c0f\u65f6"
                else -> "\u5269\u4f59 ${minutes} \u5206\u949f"
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
