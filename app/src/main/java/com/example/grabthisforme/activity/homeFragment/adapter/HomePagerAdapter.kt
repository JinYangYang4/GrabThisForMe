package com.example.grabthisforme.activity.homeFragment.adapter
import androidx.fragment.app.Fragment

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.homeFragment.view.FragmentHomeViewPager2

class HomePagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return FragmentHomeViewPager2.newInstance(position)
    }
}
