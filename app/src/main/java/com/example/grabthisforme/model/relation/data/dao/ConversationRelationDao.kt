package com.example.grabthisforme.model.relation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.relation.data.entity.ConversationParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationRelationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(entity: ConversationParticipantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(entities: List<ConversationParticipantEntity>)

    @Query("DELETE FROM conversation_participant WHERE conversationId = :conversationId AND userId = :userId")
    suspend fun deleteParticipant(conversationId: String, userId: Long)

    @Query("DELETE FROM conversation_participant WHERE conversationId = :conversationId")
    suspend fun deleteAllParticipants(conversationId: String)

    @Query("SELECT userId FROM conversation_participant WHERE conversationId = :conversationId ORDER BY sortOrder ASC")
    suspend fun getParticipantUserIds(conversationId: String): List<Long>

    @Query("SELECT * FROM conversation_participant WHERE conversationId = :conversationId ORDER BY sortOrder ASC")
    suspend fun getParticipants(conversationId: String): List<ConversationParticipantEntity>

    @Query("SELECT * FROM conversation_participant WHERE conversationId IN (:conversationIds) ORDER BY conversationId ASC, sortOrder ASC")
    suspend fun getParticipantsByConversationIds(conversationIds: List<String>): List<ConversationParticipantEntity>

    @Query("SELECT * FROM conversation_participant ORDER BY conversationId ASC, sortOrder ASC")
    fun observeAllParticipants(): Flow<List<ConversationParticipantEntity>>

    @Query("SELECT * FROM conversation_participant WHERE userId = :userId ORDER BY joinedAt DESC, conversationId ASC")
    fun observeParticipantsByUserId(userId: Long): Flow<List<ConversationParticipantEntity>>
}
