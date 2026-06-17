package com.example.grabthisforme.model.message.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.message.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM message_content WHERE messageId = :messageId LIMIT 1")
    suspend fun getMessageEntityById(messageId: String): MessageEntity?

    @Query("SELECT * FROM message_content WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessageEntitiesByConversationId(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM message_content WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageEntityByConversationId(conversationId: String): MessageEntity?

    @Query("DELETE FROM message_content WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM message_content WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: String)

    @Query("SELECT * FROM message_content WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessageEntitiesByConversationId(conversationId: String): Flow<List<MessageEntity>>
}
