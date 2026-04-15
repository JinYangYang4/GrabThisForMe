package com.example.grabthisforme.activity.communityFragment.adpter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.communityFragment.view.FragmentCommunityViewPager2


class CommunityPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return FragmentCommunityViewPager2.newInstance(position)
    }
}