package com.example.grabthisforme.model.conversation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationUserStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationUserStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(state: ConversationUserStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStates(states: List<ConversationUserStateEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStateIfAbsent(state: ConversationUserStateEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStatesIfAbsent(states: List<ConversationUserStateEntity>)

    @Query("SELECT * FROM conversation_user_state WHERE userId = :userId")
    fun observeStatesByUserId(userId: Long): Flow<List<ConversationUserStateEntity>>

    @Query("DELETE FROM conversation_user_state WHERE conversationId = :conversationId")
    suspend fun deleteStatesByConversationId(conversationId: String)

    @Query("UPDATE conversation_user_state SET isHidden = :isHidden WHERE conversationId = :conversationId AND userId = :userId")
    suspend fun updateHiddenState(conversationId: String, userId: Long, isHidden: Boolean)

    @Query("UPDATE conversation_user_state SET isHidden = :isHidden WHERE conversationId = :conversationId AND userId IN (:userIds)")
    suspend fun updateHiddenStates(conversationId: String, userIds: List<Long>, isHidden: Boolean)

    @Query("UPDATE conversation_user_state SET unreadCount = :unreadCount WHERE conversationId = :conversationId AND userId = :userId")
    suspend fun updateUnreadCount(conversationId: String, userId: Long, unreadCount: Int)

    @Query("UPDATE conversation_user_state SET lastReadTime = :lastReadTime WHERE conversationId = :conversationId AND userId = :userId")
    suspend fun updateLastReadTime(conversationId: String, userId: Long, lastReadTime: Long?)

    @Query("UPDATE conversation_user_state SET unreadCount = 0, isHidden = 0, lastReadTime = :lastReadTime WHERE conversationId = :conversationId AND userId = :userId")
    suspend fun markRead(conversationId: String, userId: Long, lastReadTime: Long?)

    @Query("UPDATE conversation_user_state SET unreadCount = unreadCount + :delta WHERE conversationId = :conversationId AND userId IN (:userIds)")
    suspend fun increaseUnreadCount(conversationId: String, userIds: List<Long>, delta: Int = 1)

    @Query("SELECT * FROM conversation_user_state WHERE conversationId = :conversationId AND userId = :userId LIMIT 1")
    suspend fun getState(conversationId: String, userId: Long): ConversationUserStateEntity?
}
