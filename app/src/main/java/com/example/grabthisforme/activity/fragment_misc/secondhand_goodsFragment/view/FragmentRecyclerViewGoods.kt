package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.view

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter.ConditionRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter.SecondhandGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.model.ConditionModel
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.viewModel.CategoryViewModel
import com.example.grabthisforme.databinding.FragmentGoodsRvBinding
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import kotlin.collections.forEach

//禁止父控件拦截触摸事件
//抢事件 / 抢手势
class FragmentRecyclerViewGoods: Fragment() {
    private var _binding: FragmentGoodsRvBinding? = null
    private lateinit var viewModel: CategoryViewModel

    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoodsRvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]
        initConditionRecyclerView()
        initRecyclerView()
    }
    @SuppressLint("ClickableViewAccessibility")
    fun initConditionRecyclerView() {

        val adapter = ConditionRecyclerViewAdapter() {
            viewModel.switchCategory(it.id)
            binding.rvGoodsCondition.smoothScrollToPosition(it.id.toInt())

        }

        binding.rvGoodsCondition.adapter = adapter
        viewModel.categoryList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(ArrayList(list))
        }
        viewModel.initCategories()
        binding.rvGoodsCondition.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }


    fun initRecyclerView(){
        val adapter = SecondhandGoodsRecyclerViewAdapter(){}
        binding.RvOrder.adapter = adapter
        binding.RvOrder.layoutManager =  StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        adapter.submitList(SecondhandGoods.SecondhandGoodsMockData.generateDefaultMockData())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
