package com.example.grabthisforme.activity.mainactivity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.ui_model.RecentUserItemUiModel
import com.example.grabthisforme.databinding.RvRecentlyUserItemBinding

class RVRecentlyUserAdapter(val clickListener : (userId : Long) -> Unit) : ListAdapter<RecentUserItemUiModel, RVRecentlyUserAdapter.ViewHolder>(UserDiffItemCallback()){
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
        val user = getItem(position)
        holder.bind(user,clickListener)
    }

    class ViewHolder(val binding: RvRecentlyUserItemBinding): RecyclerView.ViewHolder(binding.root){
        companion object{
            fun inflate(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvRecentlyUserItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }
        }
        fun bind(user : RecentUserItemUiModel,clickListener: (Long) -> Unit){
            binding.tvName.text = user.name
            binding.tvBadge.text = user.badgeText
            Glide.with(binding.ivHeadPic)
                .load(user.imageUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)
            binding.root.setOnClickListener {
                clickListener(user.userId)
            }
        }
    }
    class UserDiffItemCallback: DiffUtil.ItemCallback<RecentUserItemUiModel>(){
        override fun areItemsTheSame(
            oldItem: RecentUserItemUiModel,
            newItem: RecentUserItemUiModel
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: RecentUserItemUiModel,
            newItem: RecentUserItemUiModel
        ): Boolean {
            return oldItem == newItem
        }

    }
}
