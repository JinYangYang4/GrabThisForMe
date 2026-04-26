package com.example.grabthisforme.model.conversation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.conversation.data.entity.ConversationBundleEntity
import com.example.grabthisforme.model.conversation.data.entity.ConversationEntity
import com.example.grabthisforme.model.conversation.domain.Conversation
import com.example.grabthisforme.model.conversation.mapper.toDomain
import com.example.grabthisforme.model.conversation.mapper.toEntity

@Dao
interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversations(conversations: List<ConversationEntity>)

    @Transaction
    @Query("SELECT * FROM conversation WHERE conversationId = :conversationId LIMIT 1")
    suspend fun getConversationBundleById(conversationId: String): ConversationBundleEntity?

    @Transaction
    @Query("SELECT * FROM conversation ORDER BY lastTime DESC")
    suspend fun getAllConversationBundles(): List<ConversationBundleEntity>

    @Query("DELETE FROM conversation WHERE conversationId = :conversationId")
    suspend fun deleteConversationById(conversationId: String)

    @Transaction
    suspend fun saveConversation(conversation: Conversation) {
        upsertConversation(conversation.toEntity())
    }

    @Transaction
    suspend fun getConversationById(conversationId: String): Conversation? {
        return getConversationBundleById(conversationId)?.toDomain()
    }

    @Transaction
    suspend fun getAllConversations(): List<Conversation> {
        return getAllConversationBundles().map { it.toDomain() }
    }
}
