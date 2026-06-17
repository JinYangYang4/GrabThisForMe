package com.example.grabthisforme.activity.homeFragment.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewOrderAdapter
import com.example.grabthisforme.activity.homeFragment.ui_model.toOrderListItemUiModel
import com.example.grabthisforme.activity.homeFragment.viewModel.FragmentHomeViewModel
import com.example.grabthisforme.databinding.FragmentTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FragmentHomeViewPager2 : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskAdapter: RecyclerViewOrderAdapter
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

    fun loadAllTask() {
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.currentTaskOrders.collectLatest { orderList ->
                taskAdapter.submitList(orderList)
            }
        }
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
        initRecyclerView()
        loadData()
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initRecyclerView() {
        taskAdapter = RecyclerViewOrderAdapter(){orderId ->
            val orderBottomSheet = OrderMessageBottomSheetFragment.newInstance(orderId)
            orderBottomSheet.show(childFragmentManager, "OrderMessageBottomSheet")
        }

        binding.rvTask.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter

        }
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
