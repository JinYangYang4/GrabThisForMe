package com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.github.chrisbanes.photoview.PhotoView

class PhotoPreviewPagerAdapter(
    private val imageUris: List<String>,
    private val onImageTap: () -> Unit
) : RecyclerView.Adapter<PhotoPreviewPagerAdapter.PhotoPreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPreviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_photo_preview_page,
            parent,
            false
        )
        return PhotoPreviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhotoPreviewViewHolder, position: Int) {
        holder.bind(imageUris[position])
    }

    override fun getItemCount(): Int = imageUris.size

    override fun onViewRecycled(holder: PhotoPreviewViewHolder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    inner class PhotoPreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photoView: PhotoView = itemView.findViewById(R.id.photo_view)

        fun bind(imageUri: String) {
            Glide.with(photoView)
                .load(imageUri)
                .fitCenter()
                .into(photoView)

            photoView.setOnPhotoTapListener { _, _, _ ->
                onImageTap()
            }
        }

        fun clear() {
            photoView.setOnPhotoTapListener(null)
            Glide.with(photoView).clear(photoView)
        }
    }
}
