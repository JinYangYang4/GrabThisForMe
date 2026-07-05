package com.example.grabthisforme.model.user.data.network.api

import com.example.grabthisforme.model.goods.data.network.dto.GoodsDto
import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.post.data.network.dto.PostDto
import com.example.grabthisforme.model.store.data.network.dto.StoreDto
import com.example.grabthisforme.model.user.data.network.dto.UserBriefDto
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApi {

    @GET("api/users")
    suspend fun searchUsers(@Query("keyword") keyword: String): ApiResponse<List<UserBriefDto>>

    @GET("api/users/{userId}/posts")
    suspend fun getUserPosts(@Path("userId") userId: Long): ApiResponse<List<PostDto>>

    @GET("api/users/{userId}/likes/posts")
    suspend fun getLikedPosts(@Path("userId") userId: Long): ApiResponse<List<PostDto>>

    @GET("api/users/{userId}/likes/stores")
    suspend fun getLikedStores(@Path("userId") userId: Long): ApiResponse<List<StoreDto>>

    @GET("api/users/{userId}/likes/goods")
    suspend fun getLikedGoods(@Path("userId") userId: Long): ApiResponse<List<GoodsDto>>
}
