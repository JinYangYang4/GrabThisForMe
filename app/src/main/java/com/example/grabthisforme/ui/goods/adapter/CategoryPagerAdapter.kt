package com.example.grabthisforme.ui.goods.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

open class CategoryPagerAdapter<T>(
    fragment: Fragment,
    private val items: List<T>,
    private val fragmentFactory: (T) -> Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return fragmentFactory(items[position])
    }
}
