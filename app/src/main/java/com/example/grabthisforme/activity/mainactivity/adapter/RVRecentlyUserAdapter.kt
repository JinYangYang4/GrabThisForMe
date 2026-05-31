package com.example.grabthisforme.activity.mainactivity.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvRecentlyUserItemBinding
import com.example.grabthisforme.model.user.domain.User

class RVRecentlyUserAdapter(val clickListener : (userid : Long) -> Unit) : ListAdapter<User, RVRecentlyUserAdapter.ViewHolder>(UserDiffItemCallback()){
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
        fun bind(user : User,clickListener: (Long) -> Unit){
            binding.tvName.text = user.name
            binding.tvBadge.text = "常联系"
            Glide.with(binding.ivHeadPic)
                .load(user.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)
            binding.root.setOnClickListener {
                clickListener(user.id)
            }
        }
    }
    class UserDiffItemCallback: DiffUtil.ItemCallback<User>(){
        override fun areItemsTheSame(
            oldItem: User,
            newItem: User
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: User,
            newItem: User
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
}
