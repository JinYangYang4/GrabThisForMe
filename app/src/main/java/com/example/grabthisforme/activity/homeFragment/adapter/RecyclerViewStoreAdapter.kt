package com.example.grabthisforme.activity.homeFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.homeFragment.ui_model.HomeStoreCardUiModel
import com.example.grabthisforme.databinding.RvStoreItemBinding

class RecyclerViewStoreAdapter(
    private val clickListener: (storeId: Long) -> Unit
) : ListAdapter<HomeStoreCardUiModel, RecyclerViewStoreAdapter.ViewHolder>(
    ViewHolder.StoreDiffItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.inflateFrom(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class ViewHolder(
        private val binding: RvStoreItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private lateinit var goodsAdapter: RecyclerViewGoodsAdapter

        companion object {
            fun inflateFrom(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvStoreItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        init {
            binding.rvGoods.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                isNestedScrollingEnabled = false
                setHasFixedSize(true)
            }
        }

        fun bind(item: HomeStoreCardUiModel, clickListener: (Long) -> Unit) {
            binding.tvStore.text = item.storeName
            binding.tvSaleNumber.text = item.salesText
            binding.tvDistance.text = item.distanceText

            goodsAdapter = RecyclerViewGoodsAdapter {}
            binding.rvGoods.adapter = goodsAdapter
            goodsAdapter.submitList(item.previewGoods)

            itemView.setOnClickListener {
                clickListener.invoke(item.storeId)
            }
            itemView.alpha = 0f
            itemView.translationY = 18f
            itemView.animate().alpha(1f).translationY(0f).setDuration(260L).start()

            Glide.with(binding.root.context)
                .load(item.storeImageUrl)
                .error(R.drawable.ic_store)
                .placeholder(R.drawable.ic_store)
                .into(binding.ivStore)
        }

        class StoreDiffItemCallback : DiffUtil.ItemCallback<HomeStoreCardUiModel>() {
            override fun areItemsTheSame(
                oldItem: HomeStoreCardUiModel,
                newItem: HomeStoreCardUiModel
            ): Boolean {
                return oldItem.storeId == newItem.storeId
            }

            override fun areContentsTheSame(
                oldItem: HomeStoreCardUiModel,
                newItem: HomeStoreCardUiModel
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
