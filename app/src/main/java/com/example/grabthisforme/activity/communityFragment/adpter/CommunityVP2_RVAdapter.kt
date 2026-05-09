package com.example.grabthisforme.activity.communityFragment.adpter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.PostRvItemBinding
import com.example.grabthisforme.model.post.domain.Post
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class CommunityVP2_RVAdapter(val clickListener:(taskId : String) -> Unit) : ListAdapter<Post,
        CommunityVP2_RVAdapter.ViewHolder>(
    ViewHolder.TaskDiffItemCallback()){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder= ViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val post = getItem(position)
        holder.bind(post,clickListener)
    }
    class ViewHolder(val binding: PostRvItemBinding) : RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflateFrom(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =  PostRvItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }

        }
        fun bind(post: Post, clickListener: (String) -> Unit) {
            binding.sendTime.text = formatTimestampToDateTime(post.createTime.toLong())
            binding.contents.text = post.content
            binding.senderName.text = post.authorName
            binding.clItem.setOnClickListener {
                clickListener.invoke(post.postId)
            }
        }

        class TaskDiffItemCallback : DiffUtil.ItemCallback<Post>(){
            override fun areItemsTheSame(
                oldItem: Post,
                newItem: Post
            ): Boolean {
                return oldItem.postId == newItem.postId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Post,
                newItem: Post
            ): Boolean {
                return oldItem == newItem
            }
        }
        fun formatTimestampToDateTime(timestamp: Long): String {
            val timeInMillis = if (timestamp.toString().length == 10) timestamp * 1000 else timestamp
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(timeInMillis)
            return sdf.format(date)
        }
    }

}

