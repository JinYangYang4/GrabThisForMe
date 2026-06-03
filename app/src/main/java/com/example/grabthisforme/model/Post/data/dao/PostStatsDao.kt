package com.example.grabthisforme.model.post.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.post.data.entity.PostStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PostStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PostStatsEntity>)

    @Query("SELECT * FROM post_stats WHERE postId = :postId LIMIT 1")
    suspend fun getPostStatsEntity(postId: String): PostStatsEntity?

    @Query("SELECT * FROM post_stats WHERE postId = :postId LIMIT 1")
    fun observePostStatsEntity(postId: String): Flow<PostStatsEntity?>

    @Query("SELECT * FROM post_stats")
    fun observeAllPostStatsEntities(): Flow<List<PostStatsEntity>>
}
