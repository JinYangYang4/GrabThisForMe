package com.example.grabthisforme.activity.homeFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewOrderAdapter
import com.example.grabthisforme.databinding.FragmentReceiveOrderBinding
import com.example.grabthisforme.model.order.data.dao.OrderDao
import com.example.grabthisforme.model.order.data.mock.OrderMockData
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderStatusInfo
import com.example.grabthisforme.model.user.data.dao.UserDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentReceive_Send_HistoryOrder : Fragment() {
    private var _binding: FragmentReceiveOrderBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: RecyclerViewOrderAdapter

    @Inject
    lateinit var orderDao: OrderDao

    @Inject
    lateinit var userDao: UserDao

    companion object {
        private const val ARG_PAGE = "arg_page"
        private const val PAGE_PENDING_RECEIVE = 0
        private const val PAGE_MY_SEND = 1
        private const val PAGE_HISTORY = 2

        fun newInstance(page: Int): FragmentReceive_Send_HistoryOrder {
            return FragmentReceive_Send_HistoryOrder().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE, page)
                }
            }
        }
    }

    private val page: Int
        get() = arguments?.getInt(ARG_PAGE, PAGE_PENDING_RECEIVE) ?: PAGE_PENDING_RECEIVE

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
        lifecycleScope.launch {
            val currentUser = userDao.getCurrentUser()
            val dbOrders = orderDao.getAllOrders()
            val sourceOrders = if (dbOrders.isEmpty()) OrderMockData.getOrderList() else dbOrders
            val filteredOrders = filterByPage(sourceOrders, currentUser?.id)
            orderAdapter.userId = currentUser?.id
            orderAdapter.submitList(filteredOrders)
        }
    }

    private fun filterByPage(orders: List<Order>, currentUserId: Long?): List<Order> {
        return when (page) {
            PAGE_PENDING_RECEIVE -> {
                orders.filter {
                    it.orderStatus == OrderStatusInfo.STATUS_PENDING_RECEIPT ||
                        it.orderStatus == OrderStatusInfo.STATUS_PENDING_DELIVERY
                }
            }

            PAGE_MY_SEND -> {
                if (currentUserId == null) emptyList()
                else orders.filter { it.sender?.id == currentUserId }
            }

            PAGE_HISTORY -> orders
            else -> orders
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
