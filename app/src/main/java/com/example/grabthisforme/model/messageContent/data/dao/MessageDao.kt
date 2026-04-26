package com.example.grabthisforme.model.messageContent.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.messageContent.data.entity.MessageEntity
import com.example.grabthisforme.model.messageContent.domain.MessageContent
import com.example.grabthisforme.model.messageContent.mapper.toDomain
import com.example.grabthisforme.model.messageContent.mapper.toEntity

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

    @Transaction
    suspend fun saveMessage(conversationId: String, message: MessageContent) {
        upsertMessage(message.toEntity(conversationId))
    }

    @Transaction
    suspend fun getMessagesByConversationId(conversationId: String): List<MessageContent> {
        return getMessageEntitiesByConversationId(conversationId).map { it.toDomain() }
    }

    @Transaction
    suspend fun getLatestMessageByConversationId(conversationId: String): MessageContent? {
        return getLatestMessageEntityByConversationId(conversationId)?.toDomain()
    }
}
