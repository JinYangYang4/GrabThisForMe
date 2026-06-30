package com.example.grabthisforme.model.friendAndGroup.data.repository

import android.util.Log
import com.example.grabthisforme.model.friendAndGroup.data.network.dto.GroupDto
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import com.example.grabthisforme.model.friendAndGroup.data.network.api.FriendAndGroupApi
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class FriendAndGroupRemoteRepository @Inject constructor(
    private val friendAndGroupApi: FriendAndGroupApi
) {

    suspend fun listFriends(): Result<List<UserDto>> {
        return runCatching {
            val response = friendAndGroupApi.listFriends()
            val data = response.data
            if (response.code != 0 || data == null) {
                error(response.message.ifBlank { "List friends failed" })
            }
            data
        }
    }

    suspend fun listGroups(): Result<List<GroupDto>> {
        return runCatching {
            val response = friendAndGroupApi.listGroups()
            val data = response.data
            if (response.code != 0 || data == null) {
                error(response.message.ifBlank { "List groups failed" })
            }
            data
        }
    }

    suspend fun searchGroups(keyword: String): Result<List<GroupDto>> {
        return runCatching {
            val response = friendAndGroupApi.searchGroups(keyword)
            val data = response.data
            if (response.code != 0 || data == null) {
                error(response.message.ifBlank { "Search groups failed" })
            }
            data
        }
    }

    suspend fun addFriend(friendUserId: Long): Result<Unit> {
        return runCatching {
            val response = friendAndGroupApi.addFriend(friendUserId)
            if (response.code != 0) {
                error(response.message.ifBlank { "Add friend failed" })
            }
        }.onFailure { throwable ->
            if (throwable is HttpException) {
                val errorBody = runCatching {
                    throwable.response()?.errorBody()?.string()
                }.getOrNull()
                Log.e(
                    "AddFriendDiag",
                    "addFriend http failed: code=${throwable.code()}, friendUserId=$friendUserId, errorBody=$errorBody",
                    throwable
                )
            } else {
                Log.e(
                    "AddFriendDiag",
                    "addFriend failed: friendUserId=$friendUserId, message=${throwable.message}",
                    throwable
                )
            }
        }
    }

    suspend fun joinGroup(groupId: Long): Result<Unit> {
        return runCatching {
            val response = friendAndGroupApi.joinGroup(groupId)
            if (response.code != 0) {
                error(response.message.ifBlank { "Join group failed" })
            }
        }
    }
}
