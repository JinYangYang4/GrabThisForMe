package com.example.grabthisforme.model.post.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.post.domain.Post
import com.example.grabthisforme.model.post.mapper.toDomain
import com.example.grabthisforme.model.post.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PostEntity>)

    @Query("SELECT * FROM post_cache WHERE postId = :postId LIMIT 1")
    fun getPostEntityFlow(postId: String): Flow<PostEntity?>

    @Query("SELECT * FROM post_cache ORDER BY createTime DESC")
    fun getAllPostEntitiesFlow(): Flow<List<PostEntity>>

    @Query("DELETE FROM post_cache WHERE postId = :postId")
    suspend fun deleteById(postId: String)

    @Transaction
    suspend fun savePost(post: Post) {
        upsert(post.toEntity())
    }

    @Transaction
    suspend fun savePosts(posts: List<Post>) {
        upsertAll(posts.map { it.toEntity() })
    }

    fun getPost(postId: String): Flow<Post?> {
        return getPostEntityFlow(postId).map { it?.toDomain() }
    }

    fun getAllPosts(): Flow<List<Post>> {
        return getAllPostEntitiesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
