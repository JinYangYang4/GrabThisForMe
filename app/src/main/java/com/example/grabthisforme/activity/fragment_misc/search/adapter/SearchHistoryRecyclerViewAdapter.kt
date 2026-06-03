package com.example.grabthisforme.activity.fragment_misc.search.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.search.community.view.SearchCommunityFragment
import com.example.grabthisforme.activity.fragment_misc.search.friend.view.FragmentSearchFriendOrGroupOrConversation
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.search.view.SearchGoodsFragment
import com.example.grabthisforme.activity.fragment_misc.storeFragment.view.StoreSearchFragment
import com.example.grabthisforme.databinding.RvSearchHistoryItemBinding
private var Show = false
class SearchHistoryRecyclerViewAdapter(
    private val clickListener: (SearchContent)  -> Unit
) : ListAdapter<SearchContent, SearchHistoryRecyclerViewAdapter.ViewHolder>(
    SearchHistoryDiffItemCallback()
), SearchGoodsFragment.OnItemComponentShowListener,
    SearchCommunityFragment.OnItemComponentShowListener,
    FragmentSearchFriendOrGroupOrConversation.OnItemComponentShowListener,
    StoreSearchFragment.OnItemComponentShowListener{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = getItem(position)
        holder.bind(history, clickListener)
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onComponentShowChanged(isShow: Boolean) {
            Show = isShow
            notifyDataSetChanged()
    }

    class ViewHolder(val binding: RvSearchHistoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvSearchHistoryItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
        fun bind(search: SearchContent, clickListener:(SearchContent)  -> Unit) {
            binding.tvTag.text = search.content
            binding.root.setOnClickListener {
                clickListener.invoke(search)
            }
            if (Show){
                binding.ivDelete.visibility = View.VISIBLE
            }else{
                binding.ivDelete.visibility = View.GONE
            }

        }
    }

    class SearchHistoryDiffItemCallback : DiffUtil.ItemCallback<SearchContent>() {
        override fun areItemsTheSame(oldItem: SearchContent, newItem: SearchContent): Boolean {
            return oldItem.id== newItem.id
        }
        override fun areContentsTheSame(oldItem: SearchContent, newItem: SearchContent): Boolean {
            return oldItem.content == newItem.content && oldItem.search_time == newItem.search_time&&oldItem.id == newItem.id
        }
    }
}
