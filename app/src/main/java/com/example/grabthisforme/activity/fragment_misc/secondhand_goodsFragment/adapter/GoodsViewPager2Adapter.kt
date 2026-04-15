package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.view.FragmentRecyclerViewGoods


class GoodsViewPager2Adapter(fragment : Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 8

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentRecyclerViewGoods()
            1 -> FragmentRecyclerViewGoods()
            2 -> FragmentRecyclerViewGoods()
            else -> FragmentRecyclerViewGoods()
        }
    }
}