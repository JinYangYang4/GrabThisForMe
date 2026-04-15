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
    private val onSignClick: (SignCalendarDay) -> Unit // 签到点击回调
) : ListAdapter<SignCalendarDay, SignCalendarRecyclerViewAdapter.CalendarViewHolder>(DiffCallback) {

    inner class CalendarViewHolder(private val binding: RvCalendarItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(day: SignCalendarDay) {

            binding.tvDateReward.text = day.reward

            when {
                day.isSigned -> {
                    binding.llItem.background = binding.root.context.getDrawable(R.drawable.bg_rounded_gray)
                }
                day.isToday -> {
                    binding.llItem.background = binding.root.context.getDrawable(R.drawable.bg_rounded_gold)
                    binding.llItem.setOnClickListener { onSignClick(day) }
                }
                else -> {
                    binding.llItem.background = binding.root.context.getDrawable(R.drawable.bg_rounded_white_day)
                    binding.llItem.setOnClickListener(null)
                }
            }
            if (day.day == 6){
                binding.ivRewardIcon.setImageResource(R.drawable.ic_coupon)
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
        val day = getItem(position)
        holder.bind(day)
    }
}