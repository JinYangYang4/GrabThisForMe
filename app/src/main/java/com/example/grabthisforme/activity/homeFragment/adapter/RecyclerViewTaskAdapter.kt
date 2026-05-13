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
import com.example.grabthisforme.databinding.TaskRvItemBinding
import com.example.grabthisforme.model.order.domain.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerViewTaskAdapter(
    var userId: Long? = null,
    private val clickListener: (taskId: Long) -> Unit
) : ListAdapter<Order, RecyclerViewTaskAdapter.ViewHolder>(ViewHolder.TaskDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener, userId)
    }

    class ViewHolder(val binding: TaskRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskRvItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(order: Order, clickListener: (Long) -> Unit, userId: Long? = null) {
            binding.sendTime.text = "\u914d\u9001\u65f6\u95f4\uff1a${formatTime(order.startTime)} - ${formatTime(order.endTime)}"
            binding.timeLeft.text = formatTimeLeft(order.startTime, order.endTime)
            binding.goodsMessage.text = "\u8bf4\u660e\uff1a${order.goods.message}"
            binding.goodsName.text = "\u5546\u54c1\uff1a${order.goods.name}"
            binding.goodsPrice.text = "\u5546\u54c1\u4ef7\u683c\uff1a\uffe5${order.goods.price}"
            binding.shelfNumber.text = "\u8d27\u67b6\u53f7\uff1a${order.shelf_number}"
            binding.aimPosition.text = "\u76ee\u7684\u5730\uff1a${order.aim_position}"

            val goodsPhoto = order.goods.pic
            if (goodsPhoto.isNotBlank()) {
                binding.goodsImg.visibility = View.VISIBLE
                Glide.with(binding.root.context)
                    .load(goodsPhoto)
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

            val isBuyerSelf = order.isBuyerSelf || (userId != null && order.buyer.id == userId)
            binding.ivState.visibility = View.VISIBLE
            if (isBuyerSelf) {
                binding.llTaskItem.setBackgroundResource(R.drawable.bg_arc_gradient)
                binding.ivState.setImageResource(R.drawable.ic_wait_receive)
            } else {
                binding.llTaskItem.setBackgroundResource(R.drawable.bg_arc_gradient_green)
                binding.ivState.setImageResource(R.drawable.ic_wait_send)
            }

            itemView.setOnClickListener {
                val taskId = order.orderId.toLongOrNull() ?: 0L
                clickListener.invoke(taskId)
            }
        }

        class TaskDiffItemCallback : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem.orderId == newItem.orderId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem == newItem
            }
        }

        private fun formatTime(timeStamp: Long): String {
            return try {
                val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(timeStamp))
            } catch (e: Exception) {
                "\u65f6\u95f4\u5f02\u5e38"
            }
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
    }
}
