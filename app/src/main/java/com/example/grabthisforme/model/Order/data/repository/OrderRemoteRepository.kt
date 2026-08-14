package com.example.grabthisforme.model.order.data.repository

import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.order.data.network.api.OrderApi
import com.example.grabthisforme.model.order.data.network.api.PurchaseItemRequest
import com.example.grabthisforme.model.order.data.network.api.PurchaseRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRemoteRepository @Inject constructor(private val orderApi: OrderApi) {
    suspend fun purchase(
        clientPurchaseId: String,
        userCouponId: String?,
        items: List<PurchaseItemRequest>
    ) = requireData(orderApi.purchase(PurchaseRequest(clientPurchaseId, userCouponId, items)))

    suspend fun listPurchaseHistory() = requireData(orderApi.listPurchaseHistory())

    private fun <T> requireData(response: ApiResponse<T>): T {
        return response.data?.takeIf { response.code == 0 }
            ?: error(response.message.ifBlank { "订单请求失败" })
    }
}
