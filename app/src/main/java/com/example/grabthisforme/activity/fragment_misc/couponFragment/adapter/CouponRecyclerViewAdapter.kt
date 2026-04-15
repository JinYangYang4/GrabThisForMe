package com.example.grabthisforme.activity.fragment_misc.couponFragment.adapter

import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.Coupon
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvCouponListItemBinding

class CouponRecyclerViewAdapter (
    private val onItemClick: ((Coupon) -> Unit)? = null
) : ListAdapter<Coupon, CouponRecyclerViewAdapter.CouponListViewHolder>(DiffCallback) {

    inner class CouponListViewHolder(private val binding: RvCouponListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(coupon: Coupon) {
            binding.tvCouponDenomination.text = "${coupon.denomination}元"
            binding.tvCouponTitle.text = coupon.title
            binding.tvCouponType.text = coupon.type
            val expireTip = if (coupon.expireTime.isNotEmpty()) {
                "有效期：至${coupon.expireTime}"
            } else {
                coupon.desc.substringAfter("有效期").substringBefore("，") // 从 desc 中提取有效期
            }
            binding.tvCouponExpire.text = expireTip

            itemView.setOnClickListener {
                onItemClick?.invoke(coupon)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Coupon>() {
        override fun areItemsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Coupon, newItem: Coupon): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponListViewHolder {
        val binding =RvCouponListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CouponListViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CouponListViewHolder, position: Int) {
        val coupon = getItem(position)
        holder.bind(coupon)
    }
}