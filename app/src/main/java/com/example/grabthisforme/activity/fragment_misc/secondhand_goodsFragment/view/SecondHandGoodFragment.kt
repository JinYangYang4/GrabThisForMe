package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter.GoodsViewPager2Adapter
import com.example.grabthisforme.databinding.FragmentSecondhandGoodsBinding
import com.example.grabthisforme.model.goods.domain.Goods
import com.google.android.material.tabs.TabLayoutMediator


class SecondHandGoodFragment() : Fragment() {
    private var _binding : FragmentSecondhandGoodsBinding ?= null

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondhandGoodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        initView()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun initView(){
        binding.clSearch.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_secondHandGoodFragment_to_searchGoodsFragment)
        }
    }

    private fun initViewPager() {
        val goodsCategoryList = Goods.GoodsCategory.entries.toList()
        val adapter = GoodsViewPager2Adapter(this, goodsCategoryList)
        binding.vpGoodsContent.adapter = adapter
        TabLayoutMediator(binding.tlGoodsCategory,binding.vpGoodsContent,){tab,position ->
            tab.text = goodsCategoryList.getOrNull(position)?.desc.orEmpty()
        }.attach()
    }
}
