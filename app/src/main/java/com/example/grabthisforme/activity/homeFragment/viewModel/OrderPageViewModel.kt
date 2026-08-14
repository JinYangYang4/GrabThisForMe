package com.example.grabthisforme.activity.homeFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.homeFragment.ui_model.OrderListItemUiModel
import com.example.grabthisforme.activity.homeFragment.ui_model.toOrderListItemUiModel
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OrderPageViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    fun refresh(page: Int) {
        if (page == OrderRepository.PAGE_HISTORY) {
            viewModelScope.launch {
                runCatching { orderRepository.refreshPurchaseHistory() }
            }
        }
    }

    fun ordersByPage(page: Int): StateFlow<List<OrderListItemUiModel>> {
        return orderRepository.ordersByPage(page)
            .map { orders -> orders.map { it.toOrderListItemUiModel() } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    }
}
