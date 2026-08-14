package com.example.grabthisforme.model.coupon.data.network.api

import com.example.grabthisforme.model.coupon.data.network.dto.CouponPurchaseDto
import com.example.grabthisforme.model.coupon.data.network.dto.CouponTemplateDto
import com.example.grabthisforme.model.coupon.data.network.dto.UserCouponDto
import com.example.grabthisforme.model.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CouponApi {
    @GET("api/coupons/market")
    suspend fun listMarket(): ApiResponse<List<CouponTemplateDto>>

    @GET("api/coupons/mine")
    suspend fun listMine(): ApiResponse<List<UserCouponDto>>

    @GET("api/coupons/applicable")
    suspend fun listApplicable(
        @Query("storeId") storeId: Long,
        @Query("orderAmount") orderAmount: Double
    ): ApiResponse<List<UserCouponDto>>

    @POST("api/coupons/{templateId}/purchase")
    suspend fun purchase(
        @Path("templateId") templateId: Long,
        @Body request: BuyCouponRequest
    ): ApiResponse<CouponPurchaseDto>
}

data class BuyCouponRequest(val clientRequestId: String)
