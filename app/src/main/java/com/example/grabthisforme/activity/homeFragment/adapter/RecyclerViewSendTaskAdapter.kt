package com.example.grabthisforme.activity.homeFragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.TaskRvItemSendBinding
import com.example.grabthisforme.model.Order.Order

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerViewSendTaskAdapter(private val clickListener:(taskId : Long) -> Unit) : ListAdapter<Order, RecyclerViewSendTaskAdapter.ViewHolder>(
    SendTaskDiffItemCallback()){
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
        val task = getItem(position)
        holder.bind(task,clickListener)
    }

    class ViewHolder(val binding : TaskRvItemSendBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflate(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskRvItemSendBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
        fun bind(task: Order,clickListener: (Long) -> Unit){
            binding.sendTime.text = "配送时间: ${formatTime(task.goods.startTime)} - ${formatTime(task.goods.endTime)}"
            binding.timeLeft.text = formatTimeLeft(task.goods.startTime, task.goods.startTime)
            binding.goodsName.text = task.goods.name
            binding.goodsPrice.text = String.format(Locale.getDefault(), "￥%.2f 取货价(含商品费用)", task.goods.price)
            binding.aimPosition.text = task.goods.aim_position
            binding.shelfNumber.text = task.goods.shelf_number
        }

        private fun formatTime(timeStamp: Long): String {
            return try {
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
    class SendTaskDiffItemCallback : DiffUtil.ItemCallback<Order>(){
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
}