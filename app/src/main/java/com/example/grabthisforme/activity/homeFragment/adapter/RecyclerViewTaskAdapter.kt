package com.example.grabthisforme.activity.homeFragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.TaskRvItemBinding
import com.example.grabthisforme.model.Order.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerViewTaskAdapter( var userId: Long? = null,val clickListener:(taskId : Long) -> Unit) : ListAdapter<Order,
        RecyclerViewTaskAdapter.ViewHolder>(
    ViewHolder.TaskDiffItemCallback()){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder= ViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val order = getItem(position)
        holder.bind(order,clickListener,userId)
    }
    class ViewHolder(val binding: TaskRvItemBinding) : RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflateFrom(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskRvItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }

        }
        fun bind(order: Order, clickListener: (Long) -> Unit, userId: Long? = null) {
            binding.sendTime.text = "配送时间: ${formatTime(order.startTime)} - ${formatTime(order.endTime)}"
            binding.timeLeft.text = formatTimeLeft(order.startTime, order.endTime)
            binding.goodsName.text = order.goods.name
            binding.goodsPrice.text = String.format(Locale.getDefault(), "￥%.2f 取货价", order.goods.price)
            if (userId != null){
                if (order.buyer.id ==  userId){
                    binding.llTaskItem.setBackgroundResource(R.drawable.bg_arc_gradient)
                    binding.ivState.setImageResource(R.drawable.ic_wait_receive)
                }else{
                    binding.llTaskItem.setBackgroundResource(R.drawable.bg_arc_gradient_green)
                    binding.ivState.setImageResource(R.drawable.ic_wait_send)
                }
            }
            itemView.setOnClickListener {
                val taskId = try {
                    order.orderId.toLong()
                } catch (e: NumberFormatException) {
                    0L
                }
                clickListener.invoke(taskId)
            }

        }

        class TaskDiffItemCallback : DiffUtil.ItemCallback<Order>(){
            override fun areItemsTheSame(
                oldItem: Order,
                newItem: Order
            ): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Order,
                newItem: Order
            ): Boolean {
                return oldItem == newItem
            }
        }
        private fun formatTime(timeStamp: Long): String {
            return try {
                // 格式可自定义：如 "MM-dd HH:mm"（只显示月日时分）、"yyyy-MM-dd HH:mm:ss"（含秒）
                val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                sdf.format(Date(timeStamp))
            } catch (e: Exception) {
                "时间格式错误"
            }
        }
        private fun formatTimeLeft(startTime: Long, endTime: Long): String {
            val duration = endTime - startTime
            if (duration <= 0) return "立即送达"

            val hours = duration / (1000 * 60 * 60)
            val minutes = (duration % (1000 * 60 * 60)) / (1000 * 60)

            return when {
                hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟内送达"
                hours > 0 -> "${hours}小时内送达"
                else -> "${minutes}分钟内送达"
            }
        }

    }
}