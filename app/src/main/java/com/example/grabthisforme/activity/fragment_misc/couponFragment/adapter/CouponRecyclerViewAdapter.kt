package com.example.grabthisforme.activity.fragment_misc.couponFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.Coupon
import com.example.grabthisforme.databinding.RvCouponListItemBinding

class CouponRecyclerViewAdapter(
    private val onItemClick: ((Coupon) -> Unit)? = null
) : ListAdapter<Coupon, CouponRecyclerViewAdapter.CouponListViewHolder>(DiffCallback) {

    inner class CouponListViewHolder(private val binding: RvCouponListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(coupon: Coupon) {
            binding.tvCouponDenomination.text = "\u00a5${coupon.denomination.toInt()}"
            binding.tvCouponTitle.text = coupon.title
            binding.tvCouponType.text = coupon.type
            binding.tvCouponExpire.text = if (coupon.expireTime.isNotEmpty()) {
                "\u6709\u6548\u671f\u81f3\uff1a${coupon.expireTime}"
            } else {
                coupon.desc
            }

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
        val binding = RvCouponListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CouponListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
