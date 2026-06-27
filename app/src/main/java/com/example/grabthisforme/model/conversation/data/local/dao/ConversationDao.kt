package com.example.grabthisforme.model.conversation.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationBundleEntity
import com.example.grabthisforme.model.conversation.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Update
    suspend fun updateConversation(conversation: ConversationEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversations(conversations: List<ConversationEntity>): List<Long>

    @Transaction
    suspend fun upsertConversation(conversation: ConversationEntity) {
        if (updateConversation(conversation) == 0) {
            insertConversation(conversation)
        }
    }

    @Transaction
    suspend fun upsertConversations(conversations: List<ConversationEntity>) {
        conversations.forEach { conversation ->
            upsertConversation(conversation)
        }
    }

    @Transaction
    @Query("SELECT * FROM conversation WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getConversationBundleById(conversationId: String): ConversationBundleEntity?

    @Transaction
    @Query(
        """
        SELECT * FROM conversation
        WHERE conversationType = :conversationType AND targetId = :targetId
        LIMIT 1
        """
    )
    suspend fun getConversationBundleByTarget(
        conversationType: String,
        targetId: Long
    ): ConversationBundleEntity?

    @Transaction
    @Query("SELECT * FROM conversation ORDER BY lastTime DESC")
    suspend fun getAllConversationBundles(): List<ConversationBundleEntity>

    @Query("DELETE FROM conversation WHERE conversationId = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversationIfNotExists(conversation: ConversationEntity)

    @Transaction
    @Query("SELECT * FROM conversation ORDER BY lastTime DESC")
    fun observeAllConversationBundles(): Flow<List<ConversationBundleEntity>>

    @Query("SELECT COUNT(*) FROM conversation")
    suspend fun getConversationCount(): Int

    @Query("UPDATE conversation SET lastMessageId = :messageId, lastTime = :timestamp WHERE conversationId = :conversationId")
    suspend fun updateLastMessage(conversationId: String, messageId: String, timestamp: Long)
}
