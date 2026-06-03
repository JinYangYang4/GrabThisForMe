package com.example.grabthisforme.model.friendAndGroup.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.grabthisforme.model.friendAndGroup.data.entity.ChatGroupEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.ChatGroupWithMembersDto
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserFriendRelationEntity
import com.example.grabthisforme.model.friendAndGroup.data.entity.UserGroupRelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendAndGroupDao {

    @Upsert
    suspend fun upsertGroup(group: ChatGroupEntity)

    @Upsert
    suspend fun upsertGroups(groups: List<ChatGroupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFriendRelation(relation: UserFriendRelationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFriendRelations(relations: List<UserFriendRelationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserGroupRelation(relation: UserGroupRelationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserGroupRelations(relations: List<UserGroupRelationEntity>)

    @Query("SELECT * FROM user_friend_relation WHERE userId = :userId ORDER BY addedTime DESC")
    fun observeFriendRelationsByUserId(userId: Long): Flow<List<UserFriendRelationEntity>>

    @Query("SELECT * FROM user_friend_relation WHERE userId = :userId ORDER BY addedTime DESC")
    suspend fun getFriendRelationsByUserId(userId: Long): List<UserFriendRelationEntity>

    @Query("DELETE FROM user_friend_relation WHERE userId = :userId AND friendUserId = :friendUserId")
    suspend fun deleteFriendRelation(userId: Long, friendUserId: Long)

    @Query("SELECT * FROM chat_group ORDER BY createTime DESC")
    fun observeAllGroups(): Flow<List<ChatGroupEntity>>

    @Query("SELECT * FROM chat_group WHERE groupId IN (:groupIds)")
    fun observeGroupsByIds(groupIds: List<Long>): Flow<List<ChatGroupEntity>>

    @Query("SELECT * FROM chat_group WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: Long): ChatGroupEntity?

    @Query("SELECT * FROM user_group_relation WHERE userId = :userId ORDER BY joinedTime DESC")
    fun observeUserGroupRelationsByUserId(userId: Long): Flow<List<UserGroupRelationEntity>>

    @Query("SELECT * FROM user_group_relation WHERE userId = :userId ORDER BY joinedTime DESC")
    suspend fun getUserGroupRelationsByUserId(userId: Long): List<UserGroupRelationEntity>

    @Query("SELECT * FROM user_group_relation WHERE groupId = :groupId ORDER BY joinedTime ASC")
    fun observeUserGroupRelationsByGroupId(groupId: Long): Flow<List<UserGroupRelationEntity>>

    @Query("SELECT * FROM user_group_relation")
    fun observeAllUserGroupRelations(): Flow<List<UserGroupRelationEntity>>

    @Query("DELETE FROM user_group_relation WHERE userId = :userId AND groupId = :groupId")
    suspend fun deleteUserGroupRelation(userId: Long, groupId: Long)

    @Query("DELETE FROM chat_group WHERE groupId IN (:groupIds)")
    suspend fun deleteGroupsByIds(groupIds: List<Long>)

    @Transaction
    @Query("SELECT * FROM chat_group WHERE groupId = :groupId LIMIT 1")
    suspend fun getGroupWithMembers(groupId: Long): ChatGroupWithMembersDto?

    @Query("SELECT COUNT(*) FROM chat_group")
    suspend fun getGroupCount(): Int

    @Query("SELECT COUNT(*) FROM user_friend_relation")
    suspend fun getFriendRelationCount(): Int
}
