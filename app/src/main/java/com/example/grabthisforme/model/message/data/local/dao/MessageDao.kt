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

    @Query("SELECT * FROM message_content WHERE clientMsgId = :clientMsgId LIMIT 1")
    suspend fun getMessageEntityByClientMsgId(clientMsgId: String): MessageEntity?

    @Query("SELECT * FROM message_content WHERE serverMsgId = :serverMsgId LIMIT 1")
    suspend fun getMessageEntityByServerMsgId(serverMsgId: String): MessageEntity?

    @Query("SELECT * FROM message_content WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessageEntitiesByConversationId(conversationId: String): List<MessageEntity>

    @Query("SELECT * FROM message_content WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageEntityByConversationId(conversationId: String): MessageEntity?

    @Query("UPDATE message_content SET status = :status WHERE clientMsgId = :clientMsgId")
    suspend fun updateMessageStatus(clientMsgId: String, status: String)

    @Query(
        """
        UPDATE message_content
        SET serverMsgId = :serverMsgId,
            serverTimestamp = :serverTimestamp,
            status = :status
        WHERE clientMsgId = :clientMsgId
        """
    )
    suspend fun markMessageSent(
        clientMsgId: String,
        serverMsgId: String,
        serverTimestamp: Long?,
        status: String
    )

    @Query("SELECT COUNT(*) FROM message_content WHERE clientMsgId = :clientMsgId")
    suspend fun countByClientMsgId(clientMsgId: String): Int

    @Query("SELECT COUNT(*) FROM message_content WHERE serverMsgId = :serverMsgId")
    suspend fun countByServerMsgId(serverMsgId: String): Int

    @Query("DELETE FROM message_content WHERE clientMsgId = :clientMsgId")
    suspend fun deleteMessageByClientMsgId(clientMsgId: String)

    @Query("DELETE FROM message_content WHERE conversationId = :conversationId")
    suspend fun deleteMessagesByConversationId(conversationId: String)

    @Query("SELECT * FROM message_content WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun observeMessageEntitiesByConversationId(conversationId: String): Flow<List<MessageEntity>>
}
