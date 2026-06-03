package com.example.grabthisforme.model.relation.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.example.grabthisforme.model.conversation.data.entity.ConversationEntity
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity

@Entity(
    tableName = "conversation_participant",
    primaryKeys = ["conversationId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["conversationId"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserAccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["userId"])
    ]
)
data class ConversationParticipantEntity(
    val conversationId: String,
    val userId: Long,
    val role: String = "",
    val joinedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
