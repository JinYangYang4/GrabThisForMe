package com.example.grabthisforme.activity.communityFragment.adpter

import android.annotation.SuppressLint
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.communityFragment.custom.MaxLinesGridLayoutManager
import com.example.grabthisforme.activity.communityFragment.ui_model.PostCardUiModel
import com.example.grabthisforme.activity.fragment_misc.post_topic.adapter.ImagesRecyclerviewAdapter
import com.example.grabthisforme.databinding.PostRvItemBinding


class CommunityVP2_RVAdapter(
    private val clickListener:(taskId : String) -> Unit,
    private val onAvatarClick: (PostCardUiModel) -> Unit = {},
    private val onPostImageClick: (PostCardUiModel, Int) -> Unit = { _, _ -> }
) : ListAdapter<PostCardUiModel,
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
        holder.bind(post, clickListener, onAvatarClick, onPostImageClick)
    }
    class ViewHolder(val binding: PostRvItemBinding) : RecyclerView.ViewHolder(binding.root){
        private var currentPost: PostCardUiModel? = null
        private var currentPostImageClick: ((PostCardUiModel, Int) -> Unit)? = null
        private val imagesAdapter = ImagesRecyclerviewAdapter { position ->
            currentPost?.let { post ->
                currentPostImageClick?.invoke(post, position)
            }
        }

        init {
            binding.rvPostImages.apply {
                adapter = imagesAdapter
                layoutManager = MaxLinesGridLayoutManager(context, 3, 3)
                isNestedScrollingEnabled = false
            }
        }

        companion object{
            private const val MAX_IMAGE_COUNT = 9

            fun inflateFrom(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =  PostRvItemBinding.inflate(layoutInflater,parent,false)
                return ViewHolder(binding)
            }

        }
        fun bind(
            post: PostCardUiModel,
            clickListener: (String) -> Unit,
            onAvatarClick: (PostCardUiModel) -> Unit,
            onPostImageClick: (PostCardUiModel, Int) -> Unit
        ) {
            currentPost = post
            currentPostImageClick = onPostImageClick
            binding.sendTime.text = post.timeText
            binding.contents.text = post.contentText
            binding.senderName.text = post.authorName
            binding.labels.text = post.tagText
            binding.tvCommentCount.text = "评论"
            binding.tvLikeCount.text = "点赞"

            val visibleImages = post.imageUrls.take(MAX_IMAGE_COUNT)
            val hiddenCount = post.imageUrls.size - visibleImages.size

            if (visibleImages.isEmpty()) {
                binding.rvPostImages.visibility = View.GONE
            } else {
                binding.rvPostImages.visibility = View.VISIBLE
                imagesAdapter.submitImages(visibleImages, hiddenCount)
            }
            Glide.with(binding.root.context)
                .load(post.authorAvatarUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivAvatar)


            binding.clItem.setOnClickListener {
                clickListener.invoke(post.postId)
            }
            binding.ivAvatar.setOnClickListener {
                onAvatarClick.invoke(post)
            }
        }

        class TaskDiffItemCallback : DiffUtil.ItemCallback<PostCardUiModel>(){
            override fun areItemsTheSame(
                oldItem: PostCardUiModel,
                newItem: PostCardUiModel
            ): Boolean {
                return oldItem.postId == newItem.postId
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: PostCardUiModel,
                newItem: PostCardUiModel
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

}
