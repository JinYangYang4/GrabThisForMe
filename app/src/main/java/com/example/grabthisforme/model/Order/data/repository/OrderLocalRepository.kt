package com.example.grabthisforme.model.order.data.repository

import com.example.grabthisforme.model.order.data.local.dao.OrderDao
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderStatusInfo
import com.example.grabthisforme.model.order.mapper.toDomain
import com.example.grabthisforme.model.order.mapper.toEntity
import com.example.grabthisforme.model.user.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderLocalRepository @Inject constructor(
    private val orderDao: OrderDao,
    private val userRepository: UserRepository
) {
    companion object {
        const val PAGE_PENDING_RECEIVE = 0
        const val PAGE_MY_SEND = 1
        const val PAGE_HISTORY = 2
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sourceOrders: StateFlow<List<Order>> = orderDao.observeAllOrderEntities()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val annotatedOrders: StateFlow<List<Order>> = combine(
        sourceOrders,
        userRepository.currentUserId
    ) { orders, currentUserId ->
        orders.withBuyerSelf(currentUserId)
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val allOrderList: StateFlow<List<Order>> = annotatedOrders

    val currentOrderList: StateFlow<List<Order>> = combine(
        annotatedOrders,
        userRepository.currentUserId
    ) { orders, currentUserId ->
        orders.filter { it.isCurrentTaskOrder(currentUserId) }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val mySendOrderList: StateFlow<List<Order>> = combine(
        annotatedOrders,
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

    val historyOrderList: StateFlow<List<Order>> = annotatedOrders

    fun ordersByPage(page: Int): StateFlow<List<Order>> {
        return when (page) {
            PAGE_PENDING_RECEIVE -> currentOrderList
            PAGE_MY_SEND -> mySendOrderList
            PAGE_HISTORY -> historyOrderList
            else -> allOrderList
        }
    }

    fun getOrder(orderId: String): Flow<Order?> {
        return orderDao.observeOrderEntity(orderId).map { it?.toDomain() }
    }

    suspend fun saveOrder(order: Order) {
        orderDao.upsert(order.toEntity())
    }

    suspend fun saveOrders(orders: List<Order>) {
        orderDao.upsertAll(orders.map { it.toEntity() })
    }

    suspend fun deleteOrderById(orderId: String) {
        orderDao.deleteById(orderId)
    }

    private fun Order.isCurrentTaskOrder(currentUserId: Long?): Boolean {
        return (orderStatus == OrderStatusInfo.STATUS_PENDING_RECEIPT ||
            orderStatus == OrderStatusInfo.STATUS_PENDING_DELIVERY) &&
            (sender?.id == currentUserId || isBuyerSelf)
    }

    private fun List<Order>.withBuyerSelf(currentUserId: Long?): List<Order> {
        return map { order ->
            order.copy(isBuyerSelf = order.buyer.id == currentUserId)
        }
    }
}
