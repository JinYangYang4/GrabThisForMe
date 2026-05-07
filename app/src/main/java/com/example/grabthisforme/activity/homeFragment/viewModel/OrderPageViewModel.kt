package com.example.grabthisforme.activity.homeFragment.viewModel

import androidx.lifecycle.ViewModel
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import com.example.grabthisforme.model.order.domain.Order
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class OrderPageViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {


    fun ordersByPage(page: Int): StateFlow<List<Order>> {
        return orderRepository.ordersByPage(page)
    }


}
