package com.example.grabthisforme.activity.informationFragment.adapter

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.informationFragment.view.FragmentConversation
import com.example.grabthisforme.activity.informationFragment.view.FragmentContacts


class InformationPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FragmentConversation()
            1 -> FragmentContacts()
            else -> FragmentConversation()
        }
    }
}
