package com.example.grabthisforme.model.friendAndGroup.data.network.api

import com.example.grabthisforme.model.friendAndGroup.data.network.dto.GroupDto
import com.example.grabthisforme.model.friendAndGroup.data.network.dto.FriendRequestDto
import com.example.grabthisforme.model.network.ApiResponse
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FriendAndGroupApi {

    @GET("api/social/friends")
    suspend fun listFriends(): ApiResponse<List<UserDto>>

    @GET("api/social/friend-requests")
    suspend fun listFriendRequests(): ApiResponse<List<FriendRequestDto>>

    @GET("api/social/groups")
    suspend fun listGroups(): ApiResponse<List<GroupDto>>

    @GET("api/social/groups/search")
    suspend fun searchGroups(@Query("keyword") keyword: String): ApiResponse<List<GroupDto>>

    @POST("api/social/friends/{friendUserId}")
    suspend fun addFriend(@Path("friendUserId") friendUserId: Long): ApiResponse<Unit>

    @POST("api/social/friends/{friendUserId}/accept")
    suspend fun acceptFriend(@Path("friendUserId") friendUserId: Long): ApiResponse<Unit>

    @POST("api/social/groups/{groupId}/join")
    suspend fun joinGroup(@Path("groupId") groupId: Long): ApiResponse<Unit>
}
