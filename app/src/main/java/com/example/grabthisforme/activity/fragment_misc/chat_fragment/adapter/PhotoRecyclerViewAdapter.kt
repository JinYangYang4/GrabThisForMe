package com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.modle.PhotoItem
import com.example.grabthisforme.databinding.RvPhotoItemBinding

class PhotoRecyclerViewAdapter(
    private val onPhotoSelect: (PhotoItem, Boolean) -> Unit
) : ListAdapter<PhotoItem, PhotoRecyclerViewAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    inner class PhotoViewHolder(private val binding: RvPhotoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(photoItem: PhotoItem, onPhotoSelect: (PhotoItem, Boolean) -> Unit) {
            val context = binding.root.context
            Glide.with(context)
                .load(photoItem.uri)
                .placeholder(R.drawable.ic_back_charactor2)
                .error(R.drawable.ic_back_charactor2)
                .into(binding.ivPhoto)

            binding.ivCheck.setImageResource(
                if (photoItem.isSelected) R.drawable.ic_select
                else R.drawable.ic_unselect
            )

            binding.root.setOnClickListener {
                val newState = !photoItem.isSelected
                val newList = currentList.toMutableList()
                newList[adapterPosition] = photoItem.copy(isSelected = newState)
                submitList(newList)
                onPhotoSelect(photoItem, newState)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = RvPhotoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position), onPhotoSelect)
    }
    class PhotoDiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.uri.toString() == newItem.uri.toString()
        }
        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem == newItem
        }
    }
    fun getSelectedPhotos(): List<Uri> {
        return currentList.filter { it.isSelected }.map { it.uri }
    }
}