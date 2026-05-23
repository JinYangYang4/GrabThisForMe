package com.example.grabthisforme.activity.fragment_misc.post_topic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.databinding.RvPostImagesBinding

class ImagesRecyclerviewAdapter(
    private val clickListener: (position: Int) -> Unit
) : ListAdapter<String, ImagesRecyclerviewAdapter.ViewHolder>(
    ImageDiffCallback()
) {
    private var hiddenCount: Int = 0

    inner class ViewHolder(
        private val binding: RvPostImagesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(imageUrl: String, position: Int) {
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_add)
                .error(R.drawable.ic_add)
                .into(binding.ivPhoto)
            binding.ivPhoto.setOnClickListener {
                clickListener.invoke(position)
            }
            val showMore = hiddenCount > 0 && position == currentList.lastIndex
            binding.llMoreSize.visibility = if (showMore) android.view.View.VISIBLE else android.view.View.GONE
            if (showMore) {
                binding.tvMoreSize.text = "+$hiddenCount"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvPostImagesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    fun submitImages(images: List<String>, hiddenCount: Int) {
        this.hiddenCount = hiddenCount.coerceAtLeast(0)
        submitList(images)
    }

    private class ImageDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
}
