package com.example.grabthisforme.model.store.data.repository

import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.store.data.network.api.AssignGoodsCategoryRequest
import com.example.grabthisforme.model.store.data.network.api.CreateStoreRequest
import com.example.grabthisforme.model.store.data.network.api.StoreApi
import com.example.grabthisforme.model.store.data.network.api.UpdateStoreCategoriesRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRemoteRepository @Inject constructor(private val storeApi: StoreApi) {
    suspend fun getStore(storeId: Long) = requireData(storeApi.getStore(storeId))
    suspend fun listMyStores() = requireData(storeApi.listMyStores())
    suspend fun createStore(request: CreateStoreRequest) = requireData(storeApi.createStore(request))
    suspend fun updateCategories(storeId: Long, request: UpdateStoreCategoriesRequest) =
        requireData(storeApi.updateCategories(storeId, request))
    suspend fun assignGoodsCategory(storeId: Long, goodsId: Long, category: String?) =
        requireData(storeApi.assignGoodsCategory(storeId, goodsId, AssignGoodsCategoryRequest(category)))

    private fun <T> requireData(response: ApiResponse<T>): T {
        return response.data?.takeIf { response.code == 0 }
            ?: error(response.message.ifBlank { "店铺请求失败" })
    }
}
