package com.example.grabthisforme.activity.communityFragment.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityPagerAdapter
import com.example.grabthisforme.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator


class FragmentCommunity : Fragment() {
    private  var _binding : FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVP2()
        val targetView = view.findViewById<ImageView>(R.id.iv_Add)
        iv_Add_init(targetView.id)
        initClickListener()
    }
    fun initVP2(){
        val adapter = CommunityPagerAdapter(this)
        binding.viewpager2.adapter = adapter
        val titles = listOf(
            "最新",
            "附近",
            "搞笑",
            "吐槽",
            "分享",
            "新鲜"
        )
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
    fun initClickListener(){
        binding.llSearch.setOnClickListener {
            Log.d("test11", "iv_Add_init: ")
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_searchCommunityFragment)
        }
    }
    fun iv_Add_init(targetView : Int){
        binding.ivAdd.setOnClickListener {view ->
            val menuDialog = CommunityLeftBottomMenuDialog.newInstance(targetView)
            fragmentManager?.let { menuDialog.show(it, "left_bottom_menu") }
        }
    }
}