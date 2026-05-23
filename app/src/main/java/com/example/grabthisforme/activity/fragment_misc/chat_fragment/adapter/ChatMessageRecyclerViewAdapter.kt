package com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bigkoo.pickerview.view.WheelTime.dateFormat
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvChatMessageItemBinding
import com.example.grabthisforme.model.messageContent.domain.MessageContent
import java.util.Date

typealias OnImageClick = (MessageContent) -> Unit
class ChatMessageRecyclerViewAdapter(private val clickListener : (MessageContent) -> Unit,private val onImageClick: OnImageClick) : ListAdapter<MessageContent, ChatMessageRecyclerViewAdapter.ViewHolder>(MessageDiffCallback()) {
    inner class ViewHolder(private val binding: RvChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: MessageContent,clickListener: (MessageContent) -> Unit) {
            if (message.isMine) {
                if (message.type == MessageContent.MessageType.IMAGE){
                    binding.ivSendImage.visibility = View.VISIBLE
                    binding.tvSendContent.visibility = View.GONE
                    val context = binding.root.context
                    Glide.with(context)
                        .load(message.mediaUrl)
                        .placeholder(R.drawable.ic_back_charactor2)
                        .error(R.drawable.ic_back_charactor2)
                        .into(binding.ivSendImage)
                    binding.ivSendImage.setOnClickListener {
                        message.mediaUrl?.let {
                            onImageClick.invoke(message)
                        }
                    }
                }else{
                    binding.ivSendImage.visibility = View.GONE
                    binding.tvSendContent.visibility = View.VISIBLE
                    binding.tvSendContent.text = message.content ?: ""
                }
                binding.llSendMessage.visibility = View.VISIBLE
                binding.llReceiveMessage.visibility = View.GONE
            } else {
                if (message.type == MessageContent.MessageType.IMAGE){
                    binding.ivReceiveImage.visibility = View.VISIBLE
                    binding.tvReceiveContent.visibility = View.GONE
                    val context = binding.root.context
                    Glide.with(context)
                        .load(message.mediaUrl)
                        .placeholder(R.drawable.ic_back_charactor2)
                        .error(R.drawable.ic_back_charactor2)
                        .into(binding.ivReceiveImage)
                    binding.ivReceiveImage.setOnClickListener {
                        message.mediaUrl?.let {
                            onImageClick.invoke(message)
                        }
                    }
                }else{
                    binding.ivReceiveImage.visibility = View.GONE
                    binding.tvReceiveContent.visibility = View.VISIBLE
                    binding.tvReceiveContent.text = message.content ?: ""
                }
                binding.llReceiveMessage.visibility = View.VISIBLE
                binding.llSendMessage.visibility = View.GONE
            }
            if (message.need_show_time){
                binding.llTime.visibility = View.VISIBLE
                val timeStr = dateFormat.format(Date(message.timestamp))
                binding.tvTime.text = timeStr
                Log.d("test11", "bind: $timeStr")
            }else{
                binding.llTime.visibility = View.GONE
                Log.d("test11", "bind:")
            }
            binding.root.setOnClickListener {
                clickListener.invoke(message)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvChatMessageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position),clickListener)
    }
    class MessageDiffCallback : DiffUtil.ItemCallback<MessageContent>() {
        override fun areItemsTheSame(oldItem: MessageContent, newItem: MessageContent): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: MessageContent, newItem: MessageContent): Boolean {
            return oldItem == newItem
        }
    }
}
