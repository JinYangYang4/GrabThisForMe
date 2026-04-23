package com.example.grabthisforme.activity.informationFragment.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.informationFragment.adapter.InformationPagerAdapter
import com.example.grabthisforme.databinding.FragmentInformationBinding
import com.google.android.material.tabs.TabLayoutMediator

class                                            FragmentInformation : Fragment() {
    private var _binding : FragmentInformationBinding?=null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVP2()
        val targetView = view.findViewById<ImageView>(R.id.iv_Add)
        iv_Add_init(targetView.id)
    }
    fun initVP2(){
        Log.d("test111", "Fragment onCreateView") // 确认Fragment是否加载
        val adapter = InformationPagerAdapter(this)
        binding.viewpager2.adapter = adapter
        initClickL()

        val titles = listOf(
            "消息",
            "全部",
        )
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
    fun iv_Add_init(targetView : Int){
        binding.ivAdd.setOnClickListener {view ->
            val menuDialog = InformationLeftBottomMenuDialog.newInstance(targetView)
            fragmentManager?.let { menuDialog.show(it, "left_bottom_menu") }
        }
    }
    fun initClickL(){
        binding.llSearch.setOnClickListener {
            (requireActivity()as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_fragmentSearchFriendOrGroupOrConversation)
        }
    }
}