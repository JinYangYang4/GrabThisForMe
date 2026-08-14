package com.example.grabthisforme.model.store.data.network.api

import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.store.data.network.dto.StoreDto
import java.math.BigDecimal
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface StoreApi {
    @GET("api/stores/{storeId}")
    suspend fun getStore(@Path("storeId") storeId: Long): ApiResponse<StoreDto>

    @GET("api/stores/mine")
    suspend fun listMyStores(): ApiResponse<List<StoreDto>>

    @POST("api/stores")
    suspend fun createStore(@Body request: CreateStoreRequest): ApiResponse<StoreDto>

    @PUT("api/stores/{storeId}/categories")
    suspend fun updateCategories(
        @Path("storeId") storeId: Long,
        @Body request: UpdateStoreCategoriesRequest
    ): ApiResponse<StoreDto>

    @PUT("api/stores/{storeId}/goods/{goodsId}/category")
    suspend fun assignGoodsCategory(
        @Path("storeId") storeId: Long,
        @Path("goodsId") goodsId: Long,
        @Body request: AssignGoodsCategoryRequest
    ): ApiResponse<StoreDto>
}

data class CreateStoreRequest(
    val name: String,
    val type: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val businessHours: String? = null,
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val isOpen: Boolean = true,
    val pic: String? = null,
    val tags: List<String> = emptyList(),
    val categories: List<String> = emptyList()
)

data class UpdateStoreCategoriesRequest(
    val categories: List<String>,
    val renamedCategories: Map<String, String> = emptyMap()
)

data class AssignGoodsCategoryRequest(val category: String?)
