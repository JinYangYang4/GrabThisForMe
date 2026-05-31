package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.CouponMallItem
import com.example.grabthisforme.databinding.RvCouponMallItemBinding

class CouponMallRecyclerViewAdapter(
    private val onExchangeClick: (CouponMallItem) -> Unit
) : ListAdapter<CouponMallItem, CouponMallRecyclerViewAdapter.CouponMallViewHolder>(DiffCallback) {

    inner class CouponMallViewHolder(private val binding: RvCouponMallItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mallItem: CouponMallItem) {
            val coupon = mallItem.coupon

            binding.tvCouponTitle.text = coupon.title
            binding.tvCouponDenomination.text = "\u00a5${coupon.denomination.toInt()}"
            binding.tvCouponInfo.text = "${coupon.type} | ${coupon.desc}"
            binding.tvNeedCoin.text = "\u9700${mallItem.needCoin}\u91d1\u5e01"
            binding.tvHotTag.text = if (mallItem.isHot) "\u70ed\u95e8" else "\u63a8\u8350"

            when (mallItem.mallStatus) {
                CouponMallItem.MallCouponStatus.EXCHANGEABLE -> {
                    binding.btnExchange.isEnabled = true
                    binding.btnExchange.text = "\u5151\u6362"
                    binding.btnExchange.alpha = 1f
                }
                CouponMallItem.MallCouponStatus.SOLD_OUT -> {
                    binding.btnExchange.isEnabled = false
                    binding.btnExchange.text = "\u5df2\u552e\u7f44"
                    binding.btnExchange.alpha = 0.45f
                }
            }
            binding.btnExchange.setOnClickListener {
                if (mallItem.mallStatus == CouponMallItem.MallCouponStatus.EXCHANGEABLE) {
                    onExchangeClick(mallItem)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CouponMallItem>() {
        override fun areItemsTheSame(oldItem: CouponMallItem, newItem: CouponMallItem): Boolean {
            return oldItem.coupon.id == newItem.coupon.id
        }

        override fun areContentsTheSame(oldItem: CouponMallItem, newItem: CouponMallItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponMallViewHolder {
        val binding = RvCouponMallItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CouponMallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponMallViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
