package com.example.grabthisforme.model.order.data.repository

import com.example.grabthisforme.model.order.data.dao.OrderDao
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderStatusInfo
import com.example.grabthisforme.model.user.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val userRepository: UserRepository
) {
    companion object {
        const val PAGE_PENDING_RECEIVE = 0
        const val PAGE_MY_SEND = 1
        const val PAGE_HISTORY = 2
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sourceOrders: StateFlow<List<Order>> = orderDao.getAllOrders()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allOrderList: StateFlow<List<Order>> = sourceOrders

    val currentOrderList: StateFlow<List<Order>> = combine(
        sourceOrders,
        userRepository.currentUserId
    ) { orders, currentUserId ->
        orders.filter { it.isCurrentTaskOrder(currentUserId) }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val mySendOrderList: StateFlow<List<Order>> = combine(
        sourceOrders,
        userRepository.currentUserId
    ) { orders, currentUserId ->
        if (currentUserId == null) {
            emptyList()
        } else {
            orders.filter { it.sender?.id == currentUserId }
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val historyOrderList: StateFlow<List<Order>> = sourceOrders

    fun ordersByPage(page: Int): StateFlow<List<Order>> {
        return when (page) {
            PAGE_PENDING_RECEIVE -> currentOrderList
            PAGE_MY_SEND -> mySendOrderList
            PAGE_HISTORY -> historyOrderList
            else -> allOrderList
        }
    }

    private fun Order.isCurrentTaskOrder(currentUserId: Long?): Boolean {
        return (orderStatus == OrderStatusInfo.STATUS_PENDING_RECEIPT ||
            orderStatus == OrderStatusInfo.STATUS_PENDING_DELIVERY) &&
                (sender?.id == currentUserId || buyer.id == currentUserId)
    }
}
