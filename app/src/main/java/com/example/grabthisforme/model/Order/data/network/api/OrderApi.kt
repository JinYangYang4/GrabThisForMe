package com.example.grabthisforme.model.order.data.network.api

import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.order.data.network.dto.PurchaseRecordDto
import com.example.grabthisforme.model.order.data.network.dto.PurchaseResultDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OrderApi {
    @POST("api/orders/purchase")
    suspend fun purchase(@Body request: PurchaseRequest): ApiResponse<PurchaseResultDto>

    @GET("api/orders/purchases")
    suspend fun listPurchaseHistory(): ApiResponse<List<PurchaseRecordDto>>
}

data class PurchaseRequest(
    val clientPurchaseId: String,
    val userCouponId: String? = null,
    val items: List<PurchaseItemRequest>
)

data class PurchaseItemRequest(
    val goodsId: Long,
    val quantity: Int
)
