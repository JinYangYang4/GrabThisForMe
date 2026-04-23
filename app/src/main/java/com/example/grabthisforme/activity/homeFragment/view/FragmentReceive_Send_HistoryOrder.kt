package com.example.grabthisforme.activity.homeFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewOrderAdapter
import com.example.grabthisforme.databinding.FragmentReceiveOrderBinding
import com.example.grabthisforme.model.Order.Order

class FragmentReceive_Send_HistoryOrder : Fragment() {
    private var _binding: FragmentReceiveOrderBinding? = null
    private val binding get() = _binding!!


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
    }

    private fun initRecyclerView() {


        val orderAdapter = RecyclerViewOrderAdapter(userId = 0){taskId ->
            val orderBottomSheet = OrderMessageBottomSheetFragment.newInstance(taskId.toString())
            orderBottomSheet.show(childFragmentManager, "OrderMessageBottomSheet")
        }


        binding.RvOrder.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
            setHasFixedSize(true)
        }

        val orderList = Order.getOrderList()
        orderAdapter.submitList(orderList)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}