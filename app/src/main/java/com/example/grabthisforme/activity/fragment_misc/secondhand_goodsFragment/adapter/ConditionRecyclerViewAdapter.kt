package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.model.ConditionModel
import com.example.grabthisforme.databinding.RvConditionItemBinding


class ConditionRecyclerViewAdapter(
    private val clickListener: (condition: ConditionModel) -> Unit
) : ListAdapter<ConditionModel, ConditionRecyclerViewAdapter.ViewHolder>(
    ConditionDiffItemCallback()
) {
    private var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val condition = getItem(position)
        holder.bind(condition, clickListener)
        Log.d("test11", "onBindViewHolder: $position")
    }

    class ViewHolder(val binding: RvConditionItemBinding) : RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = RvConditionItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(
            condition: ConditionModel,
            clickListener: (ConditionModel) -> Unit,
        ) {
            binding.tvCondition.text = condition.conditionText
            updateSelectedStyle(condition.isSelected)
            itemView.setOnClickListener {
                clickListener(condition)
                Log.d("test11", "bind: ")
                (itemView.parent.parent as? RecyclerView)?.adapter?.let { adapter ->
                    (adapter as ConditionRecyclerViewAdapter).apply {
                        selectedPosition = adapterPosition
                        notifyItemRangeChanged(0, adapter.itemCount)
                    }
                }
            }
        }

        private fun updateSelectedStyle(isSelected: Boolean) {
            if (isSelected) {
                binding.root.backgroundTintList = itemView.context.resources.getColorStateList(
                    com.example.grabthisforme.R.color.green_light,
                    itemView.context.theme
                )
                binding.tvCondition.setTextColor(
                    itemView.context.resources.getColor(
                        com.example.grabthisforme.R.color.gray_mid_light,
                        itemView.context.theme
                    )
                )
            } else {
                binding.root.backgroundTintList = itemView.context.resources.getColorStateList(
                    com.example.grabthisforme.R.color.gray_mid_light,
                    itemView.context.theme
                )
                binding.tvCondition.setTextColor(
                    itemView.context.resources.getColor(
                        com.example.grabthisforme.R.color.black,
                        itemView.context.theme
                    )
                )
            }
        }
    }

    class ConditionDiffItemCallback : DiffUtil.ItemCallback<ConditionModel>() {
        override fun areItemsTheSame(oldItem: ConditionModel, newItem: ConditionModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConditionModel, newItem: ConditionModel): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.conditionText == newItem.conditionText &&
                    oldItem.isSelected == newItem.isSelected
        }
    }


    fun resetSelectedState() {
        selectedPosition = -1
        notifyItemRangeChanged(0, itemCount)
    }
}