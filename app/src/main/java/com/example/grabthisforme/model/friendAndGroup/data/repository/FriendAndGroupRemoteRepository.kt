package com.example.grabthisforme.model.friendAndGroup.data.repository

import com.example.grabthisforme.model.friendAndGroup.data.network.dto.GroupDto
import com.example.grabthisforme.model.user.data.network.dto.UserDto
import com.example.grabthisforme.model.friendAndGroup.data.network.api.FriendAndGroupApi
import javax.inject.Inject
import javax.inject.Singleton

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

    suspend fun addFriend(friendUserId: Long): Result<Unit> {
        return runCatching {
            val response = friendAndGroupApi.addFriend(friendUserId)
            if (response.code != 0) {
                error(response.message.ifBlank { "Add friend failed" })
            }
        }
    }
}
