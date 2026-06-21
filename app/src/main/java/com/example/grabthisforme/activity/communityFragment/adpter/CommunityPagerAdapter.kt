package com.example.grabthisforme.activity.communityFragment.adpter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.communityFragment.model.CommunityTabs
import com.example.grabthisforme.activity.communityFragment.view.FragmentCommunityViewPager2


class CommunityPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = CommunityTabs.items.size

    override fun createFragment(position: Int): Fragment {
        val spec = CommunityTabs.items[position]
        return FragmentCommunityViewPager2.newInstance(
            title = spec.title,
            mode = spec.mode.name,
            categoryKey = spec.categoryKey
        )
    }
}
