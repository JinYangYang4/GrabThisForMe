package com.example.grabthisforme.activity.homeFragment.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.homeFragment.view.FragmentReceive_Send_HistoryOrder

class OrderDetailViewPager2Adapter(fragment : Fragment) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentReceive_Send_HistoryOrder.newInstance(0)
            1 -> FragmentReceive_Send_HistoryOrder.newInstance(1)
            2 -> FragmentReceive_Send_HistoryOrder.newInstance(2)
            else ->  FragmentReceive_Send_HistoryOrder.newInstance(2)
        }
    }
}
