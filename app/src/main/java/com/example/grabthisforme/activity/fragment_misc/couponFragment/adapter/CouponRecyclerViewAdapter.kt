package com.example.grabthisforme.activity.fragment_misc.couponFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.couponFragment.viewModel.CouponDisplayItem
import com.example.grabthisforme.databinding.RvCouponListItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CouponRecyclerViewAdapter(
    private val onAction: (CouponDisplayItem) -> Unit
) : ListAdapter<CouponDisplayItem, CouponRecyclerViewAdapter.CouponListViewHolder>(DiffCallback) {

    inner class CouponListViewHolder(private val binding: RvCouponListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CouponDisplayItem) = with(binding) {
            tvCouponDenomination.text = "¥${money(item.discountAmount)}"
            tvCouponTitle.text = item.title
            tvCouponType.text = if (item.minimumAmount > 0) {
                "满¥${money(item.minimumAmount)}可用"
            } else {
                "无门槛"
            }
            tvCouponExpire.text = when {
                item.isMarket -> "售价 ¥${money(item.purchasePrice)} · 有效期 ${item.validDays} 天 · 库存 ${item.stock}"
                item.validUntil != null -> "有效期至 ${DATE_FORMAT.format(Date(item.validUntil))}"
                else -> item.description
            }
            tvCouponAction.text = when {
                item.isMarket && item.canPurchase -> "购买 ¥${money(item.purchasePrice)}"
                item.isMarket -> "不可购买"
                item.status == "AVAILABLE" -> "可使用"
                item.status == "USED" -> "已使用"
                else -> "已过期"
            }
            tvCouponAction.isEnabled = item.isMarket && item.canPurchase
            tvCouponAction.alpha = if (tvCouponAction.isEnabled) 1f else 0.55f
            tvCouponAction.setOnClickListener { onAction(item) }
            root.setOnClickListener { if (item.isMarket && item.canPurchase) onAction(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CouponListViewHolder(
        RvCouponListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: CouponListViewHolder, position: Int) = holder.bind(getItem(position))

    companion object DiffCallback : DiffUtil.ItemCallback<CouponDisplayItem>() {
        private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        private fun money(value: Double) = if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.getDefault(), "%.2f", value)
        override fun areItemsTheSame(oldItem: CouponDisplayItem, newItem: CouponDisplayItem) = oldItem.key == newItem.key
        override fun areContentsTheSame(oldItem: CouponDisplayItem, newItem: CouponDisplayItem) = oldItem == newItem
    }
}
