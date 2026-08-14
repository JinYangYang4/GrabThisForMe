package com.example.grabthisforme.model.goods.data.network.api

import com.example.grabthisforme.model.goods.data.network.dto.GoodsDto
import com.example.grabthisforme.model.network.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GoodsApi {
    @GET("api/goods/{goodsId}")
    suspend fun getGoods(@Path("goodsId") goodsId: Long): ApiResponse<GoodsDto>

    @POST("api/goods")
    suspend fun createGoods(@Body request: CreateGoodsRequest): ApiResponse<GoodsDto>
}

data class CreateGoodsRequest(
    val storeId: Long,
    val name: String,
    val message: String,
    val categoryKey: String?,
    val price: Double,
    val discountPrice: Double,
    val discountTag: String,
    val pic: String,
    val tag: String,
    val unit: String,
    val stock: Int,
    val isHot: Boolean = false,
    val secondhand: Boolean = false
)
