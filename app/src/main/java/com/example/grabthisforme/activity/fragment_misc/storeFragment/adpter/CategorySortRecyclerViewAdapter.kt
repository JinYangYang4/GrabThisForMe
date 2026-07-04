package com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.databinding.RvCategorySortItemBinding
import java.util.Collections

class CategorySortRecyclerViewAdapter :
    RecyclerView.Adapter<CategorySortRecyclerViewAdapter.ViewHolder>() {

    private val items = mutableListOf<String>()

    init {
        setHasStableIds(true)
    }

    fun submitList(list: List<String>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition !in items.indices || toPosition !in items.indices) return
        if (fromPosition == toPosition) return
        if (fromPosition < toPosition) {
            for (index in fromPosition until toPosition) {
                Collections.swap(items, index, index + 1)
            }
        } else {
            for (index in fromPosition downTo toPosition + 1) {
                Collections.swap(items, index, index - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    fun getCurrentItems(): List<String> = items.toList()

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.inflate(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(private val binding: RvCategorySortItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: String) {
            binding.tvCategoryName.text = category
        }

        companion object {
            fun inflate(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = RvCategorySortItemBinding.inflate(inflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

