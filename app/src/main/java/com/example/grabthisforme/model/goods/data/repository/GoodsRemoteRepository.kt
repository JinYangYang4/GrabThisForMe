package com.example.grabthisforme.model.goods.data.repository

import com.example.grabthisforme.model.goods.data.network.api.CreateGoodsRequest
import com.example.grabthisforme.model.goods.data.network.api.GoodsApi
import com.example.grabthisforme.model.goods.data.network.dto.GoodsDto
import com.example.grabthisforme.model.network.ApiResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoodsRemoteRepository @Inject constructor(private val goodsApi: GoodsApi) {
    suspend fun createGoods(request: CreateGoodsRequest): GoodsDto = requireData(goodsApi.createGoods(request))
    suspend fun getGoods(goodsId: Long): GoodsDto = requireData(goodsApi.getGoods(goodsId))

    private fun <T> requireData(response: ApiResponse<T>): T {
        return response.data?.takeIf { response.code == 0 }
            ?: error(response.message.ifBlank { "商品请求失败" })
    }
}
