package com.example.grabthisforme.activity.homeFragment.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewOrderAdapter
import com.example.grabthisforme.activity.homeFragment.viewModel.FragmentHomeViewModel
import com.example.grabthisforme.databinding.FragmentTaskBinding
import com.example.grabthisforme.model.order.data.mock.OrderMockData
import com.example.grabthisforme.model.order.domain.Order


class FragmentHomeViewPager2 : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: RecyclerViewOrderAdapter
    private  var mockList : List<Order> ?= null
    private lateinit var homeViewModel: FragmentHomeViewModel

    companion object {
        private const val KEY_TYPE = "task_type"

        fun newInstance(type: Int): FragmentHomeViewPager2 {
            return FragmentHomeViewPager2().apply {
                arguments = Bundle().apply {
                    putInt(KEY_TYPE, type)
                }
            }
        }
    }

    private val taskType: Int by lazy {
        arguments?.getInt(KEY_TYPE) ?: 0
    }

    fun loadAllTask(){
        mockList = OrderMockData.getOrderList()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(requireActivity()).get(FragmentHomeViewModel::class.java)

        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadData()
        initRecyclerView()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initRecyclerView() {
        taskAdapter = RecyclerViewOrderAdapter(){taskId ->
            val orderBottomSheet = OrderMessageBottomSheetFragment.newInstance(taskId.toString())
            orderBottomSheet.show(childFragmentManager, "OrderMessageBottomSheet")
        }

        binding.rvTask.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter

        }
        taskAdapter.submitList(OrderMockData.getOrderList())
    }
    private fun loadData() {
        when (taskType) {
            0 -> loadAllTask()
            1 -> loadAllTask()
            2 -> loadAllTask()
            3 -> loadAllTask()
            4 -> loadAllTask()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
