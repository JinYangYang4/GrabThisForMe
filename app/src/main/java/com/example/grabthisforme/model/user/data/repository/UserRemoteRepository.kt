package com.example.grabthisforme.model.user.data.repository

import com.example.grabthisforme.model.goods.data.network.dto.GoodsDto
import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.user.data.network.api.UserApi
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import com.example.grabthisforme.model.post.data.network.dto.PostDto
import com.example.grabthisforme.model.store.data.network.dto.StoreDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteRepository @Inject constructor(
    private val userApi: UserApi
) {

    suspend fun searchUsers(keyword: String): Result<List<UserDto>> {
        return runCatching {
            requireSuccessfulData(userApi.searchUsers(keyword))
        }
    }

    suspend fun getUserPosts(userId: Long): Result<List<PostDto>> {
        return runCatching {
            requireSuccessfulData(userApi.getUserPosts(userId))
        }
    }

    suspend fun getLikedPosts(userId: Long): Result<List<PostDto>> {
        return runCatching {
            requireSuccessfulData(userApi.getLikedPosts(userId))
        }
    }

    suspend fun getLikedStores(userId: Long): Result<List<StoreDto>> {
        return runCatching {
            requireSuccessfulData(userApi.getLikedStores(userId))
        }
    }

    suspend fun getLikedGoods(userId: Long): Result<List<GoodsDto>> {
        return runCatching {
            requireSuccessfulData(userApi.getLikedGoods(userId))
        }
    }

    private fun <T> requireSuccessfulData(response: ApiResponse<T>): T {
        val data = response.data
        if (response.code != 0 || data == null) {
            error(response.message.ifBlank { "Network request failed" })
        }
        return data
    }
}
