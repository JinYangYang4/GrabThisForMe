package com.example.grabthisforme.activity.homeFragment.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.MainActivity.viewModel.MainViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewOrderAdapter
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewSendTaskAdapter
import com.example.grabthisforme.databinding.FragmentHome1Binding
import com.example.grabthisforme.model.Order.Order



class FragmentHome1 : Fragment() {
    private var _binding : FragmentHome1Binding ?= null
    private val binding get() = _binding!!
    private var llSendOrderTop = 0
    private var isRvOrderAtTop = false
    private lateinit var sharedViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHome1Binding.inflate(inflater,container,false)
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.YGet.setOnClickListener {
            sharedViewModel.toPage(0)
        }
        binding.ivHeadPic.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
        initRvSendTask()
        initRvOrder()
        return binding.root
    }
    @SuppressLint("ClickableViewAccessibility")
    fun initRvSendTask(){
        val taskList = Order.getOrderList()
        val adapter1 = RecyclerViewSendTaskAdapter(){
        }
        binding.rvTask.adapter = adapter1
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        taskList.let {
            adapter1.submitList(it)
        }
        binding.rvTask.isNestedScrollingEnabled = false
        binding.rvTask.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    fun initRvOrder(){
        val orderList = Order.getOrderList()
        val adapter2 = RecyclerViewOrderAdapter(){}
        binding.rvOrder.adapter = adapter2
        binding.rvOrder.layoutManager = LinearLayoutManager(requireContext())
        orderList.let {
            adapter2.submitList(orderList)
        }
    }


}