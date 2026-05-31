package com.example.grabthisforme.activity.fragment_misc.searchFragment.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.fragment_misc.searchFragment.view.FragmentRecommendation

class SearchRecommendationViewPager2Adapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = FragmentRecommendation.RecommendationCategory.entries.size

    override fun createFragment(position: Int): Fragment {
        return FragmentRecommendation.newInstance(position)
    }
}
