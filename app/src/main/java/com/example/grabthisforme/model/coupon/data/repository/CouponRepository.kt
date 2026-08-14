package com.example.grabthisforme.model.coupon.data.repository

import com.example.grabthisforme.model.coupon.data.network.api.BuyCouponRequest
import com.example.grabthisforme.model.coupon.data.network.api.CouponApi
import com.example.grabthisforme.model.network.ApiResponse
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponRepository @Inject constructor(private val couponApi: CouponApi) {
    suspend fun listMarket() = requireData(couponApi.listMarket())

    suspend fun listMine() = requireData(couponApi.listMine())

    suspend fun listApplicable(storeId: Long, orderAmount: Double) =
        requireData(couponApi.listApplicable(storeId, orderAmount))

    suspend fun purchase(templateId: Long) = requireData(
        couponApi.purchase(templateId, BuyCouponRequest(UUID.randomUUID().toString()))
    )

    private fun <T> requireData(response: ApiResponse<T>): T =
        response.data?.takeIf { response.code == 0 }
            ?: error(response.message.ifBlank { "优惠券请求失败" })
}
