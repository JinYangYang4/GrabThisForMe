package com.example.grabthisforme.model.order.data.repository

import com.example.grabthisforme.model.order.domain.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val localRepository: OrderLocalRepository,
    private val remoteRepository: OrderRemoteRepository
) {
    companion object {
        const val PAGE_PENDING_RECEIVE = OrderLocalRepository.PAGE_PENDING_RECEIVE
        const val PAGE_MY_SEND = OrderLocalRepository.PAGE_MY_SEND
        const val PAGE_HISTORY = OrderLocalRepository.PAGE_HISTORY
    }

    val allOrderList: StateFlow<List<Order>> = localRepository.allOrderList
    val currentOrderList: StateFlow<List<Order>> = localRepository.currentOrderList
    val mySendOrderList: StateFlow<List<Order>> = localRepository.mySendOrderList
    val historyOrderList: StateFlow<List<Order>> = localRepository.historyOrderList

    fun ordersByPage(page: Int): StateFlow<List<Order>> {
        return localRepository.ordersByPage(page)
    }

    fun getOrder(orderId: String): Flow<Order?> {
        return localRepository.getOrder(orderId)
    }

    suspend fun saveOrder(order: Order) {
        localRepository.saveOrder(order)
    }

    suspend fun saveOrders(orders: List<Order>) {
        localRepository.saveOrders(orders)
    }

    suspend fun deleteOrderById(orderId: String) {
        localRepository.deleteOrderById(orderId)
    }
}
