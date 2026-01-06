package com.example.grabthisforme.activity.homeFragment.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvOrderItemBinding
import com.example.grabthisforme.model.Order.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecyclerViewOrderAdapter(private val clickListener:(taskId : Long )-> Unit): ListAdapter<Order, RecyclerViewOrderAdapter.ViewHolder>(
    ViewHolder.OrderDiffItemCallback()) {
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
        val order = getItem(position)
        return holder.bind(order,clickListener)
    }

    class ViewHolder(val binding: RvOrderItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflate(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvOrderItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
        fun bind(order : Order,clickListener: (Long) -> Unit){
            binding.goodsPrice.text = order.goods.price.toString()
            binding.shelfNumber.text = order.goods.shelf_number
            binding.aimPosition.text = order.goods.aim_position
            binding.goodsName.text = order.goods.name
            binding.sendTime.text = "配送时间: ${formatTime(order.goods.startTime)} - ${formatTime(order.goods.endTime)}"
            binding.timeLeft.text =  formatTimeLeft(order.goods.startTime, order.goods.endTime)
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
        class OrderDiffItemCallback : DiffUtil.ItemCallback<Order>(){
            override fun areItemsTheSame(
                oldItem: Order,
                newItem: Order
            ): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Order,
                newItem: Order
            ): Boolean {
                return oldItem.orderId == newItem.orderId
            }

        }
    }

}