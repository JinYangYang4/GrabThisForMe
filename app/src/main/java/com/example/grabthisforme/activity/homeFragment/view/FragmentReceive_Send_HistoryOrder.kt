package com.example.grabthisforme.activity.homeFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewOrderAdapter
import com.example.grabthisforme.activity.homeFragment.viewModel.OrderPageViewModel
import com.example.grabthisforme.databinding.FragmentReceiveOrderBinding
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentReceive_Send_HistoryOrder : Fragment() {
    private var _binding: FragmentReceiveOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: RecyclerViewOrderAdapter
    private val viewModel: OrderPageViewModel by viewModels()

    companion object {
        private const val ARG_PAGE = "arg_page"

        fun newInstance(page: Int): FragmentReceive_Send_HistoryOrder {
            return FragmentReceive_Send_HistoryOrder().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE, page)
                }
            }
        }
    }

    private val page: Int
        get() = arguments?.getInt(ARG_PAGE, OrderRepository.PAGE_PENDING_RECEIVE)
            ?: OrderRepository.PAGE_PENDING_RECEIVE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiveOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        loadOrders()
    }

    private fun initRecyclerView() {
        orderAdapter = RecyclerViewOrderAdapter(userId = null) { taskId ->
            val orderBottomSheet = OrderMessageBottomSheetFragment.newInstance(taskId.toString())
            orderBottomSheet.show(childFragmentManager, "OrderMessageBottomSheet")
        }

        binding.RvOrder.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.ordersByPage(page).collectLatest { orderList ->
                orderAdapter.submitList(orderList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
