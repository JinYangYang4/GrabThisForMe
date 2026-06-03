package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.SignCalendarDay
import com.example.grabthisforme.databinding.RvCalendarItemBinding

class SignCalendarRecyclerViewAdapter(
    private val onSignClick: (SignCalendarDay) -> Unit
) : ListAdapter<SignCalendarDay, SignCalendarRecyclerViewAdapter.CalendarViewHolder>(DiffCallback) {

    inner class CalendarViewHolder(private val binding: RvCalendarItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(day: SignCalendarDay) {
            binding.tvDateReward.text = day.reward
            binding.ivRewardIcon.setImageResource(
                if (day.reward.contains("优惠券")) R.drawable.ic_coupon else R.drawable.ic_coin
            )

            when {
                day.isSigned -> {
                    binding.llItem.background = binding.root.context.getDrawable(R.drawable.bg_sign_signed)
                    binding.llItem.alpha = 0.85f
                    binding.llItem.setOnClickListener(null)
                }

                day.isToday -> {
                    binding.llItem.background = binding.root.context.getDrawable(R.drawable.bg_sign_today)
                    binding.llItem.alpha = 1f
                    binding.llItem.setOnClickListener { onSignClick(day) }
                }

                else -> {
                    binding.llItem.background = binding.root.context.getDrawable(R.drawable.bg_sign_future)
                    binding.llItem.alpha = 1f
                    binding.llItem.setOnClickListener(null)
                }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SignCalendarDay>() {
        override fun areItemsTheSame(oldItem: SignCalendarDay, newItem: SignCalendarDay): Boolean {
            return oldItem.day == newItem.day
        }

        override fun areContentsTheSame(oldItem: SignCalendarDay, newItem: SignCalendarDay): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = RvCalendarItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
