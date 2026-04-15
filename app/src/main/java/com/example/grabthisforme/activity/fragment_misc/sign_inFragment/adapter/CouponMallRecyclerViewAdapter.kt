package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.CouponMallItem
import com.example.grabthisforme.databinding.RvCouponMallItemBinding

class CouponMallRecyclerViewAdapter(
    private val onExchangeClick: (CouponMallItem) -> Unit
) : ListAdapter<CouponMallItem, CouponMallRecyclerViewAdapter.CouponMallViewHolder>(DiffCallback) {
    inner class CouponMallViewHolder(private val binding: RvCouponMallItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(mallItem: CouponMallItem) {
            val coupon = mallItem.coupon

            // 1. 绑定核心字段
            binding.tvCouponTitle.text = coupon.title
            binding.tvCouponDenomination.text = "${coupon.denomination}元"
            binding.tvCouponInfo.text = "${coupon.type} | ${coupon.desc}"
            binding.tvNeedCoin.text = "需${mallItem.needCoin}金币"

            if (mallItem.isHot) {
                binding.tvHotTag.text = "热门"
                binding.tvHotTag.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.red_light))
            } else {
                binding.tvHotTag.text = "推荐"
                binding.tvHotTag.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.gray_translucent))
            }

            // 3. 绑定商城状态（可兑换/已售罄）
            when (mallItem.mallStatus) {
                CouponMallItem.MallCouponStatus.EXCHANGEABLE -> {
                    binding.btnExchange.isEnabled = true
                    binding.btnExchange.text = "兑换"
                    binding.btnExchange.setBackgroundColor(binding.root.context.getColor(android.R.color.holo_blue_light))
                }
                CouponMallItem.MallCouponStatus.SOLD_OUT -> {
                    binding.btnExchange.isEnabled = false
                    binding.btnExchange.text = "已售罄"
                    binding.btnExchange.setBackgroundColor(binding.root.context.getColor(android.R.color.darker_gray))
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
        val mallItem = getItem(position)
        holder.bind(mallItem)
    }
}