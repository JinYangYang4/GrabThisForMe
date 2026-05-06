package com.example.grabthisforme.model.Order.data.repository

import com.example.grabthisforme.model.order.domain.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OrderRepository {
    private val _currentOrderList = MutableStateFlow< List<Order>?>(emptyList())
    val currentOrderList : StateFlow<List<Order>?> = _currentOrderList.asStateFlow()
}