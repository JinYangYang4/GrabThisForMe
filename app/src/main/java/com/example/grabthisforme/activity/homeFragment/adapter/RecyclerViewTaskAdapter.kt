package com.example.grabthisforme.activity.homeFragment.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.databinding.TaskRvItemBinding
import com.example.grabthisforme.model.rv_task.RecyclerviewTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerViewTaskAdapter(val clickListener:(taskId : Long) -> Unit) : ListAdapter<RecyclerviewTask,
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
        val task = getItem(position)
        holder.bind(task,clickListener)
    }
    class ViewHolder(val binding: TaskRvItemBinding) : RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflateFrom(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TaskRvItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }

        }
        fun bind(task: RecyclerviewTask, clickListener: (Long) -> Unit) {
            binding.task = task
            binding.sendTime.text = "配送时间: ${formatTime(task.startTime)} - ${formatTime(task.endTime)}"
            binding.timeLeft.text = formatTimeLeft(task.startTime, task.endTime)
            binding.goodsName.text = task.name
            binding.goodsPrice.text = String.format(Locale.getDefault(), "￥%.2f 取货价", task.price)

        }

        class TaskDiffItemCallback : DiffUtil.ItemCallback<RecyclerviewTask>(){
            override fun areItemsTheSame(
                oldItem: RecyclerviewTask,
                newItem: RecyclerviewTask
            ): Boolean {
                return oldItem == newItem
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: RecyclerviewTask,
                newItem: RecyclerviewTask
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