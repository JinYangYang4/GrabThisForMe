package com.example.grabthisforme.model.post.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.post.data.entity.PostCommentEntity
import com.example.grabthisforme.model.post.data.entity.PostEntity
import com.example.grabthisforme.model.post.data.entity.PostReplyEntity
import com.example.grabthisforme.model.post.data.entity.PostWithAuthorEntity
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PostEntity>)

    @Query("SELECT * FROM post_cache ORDER BY createTime DESC")
    fun observeAllPostEntities(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthorAccountIfAbsent(account: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthorAccountsIfAbsent(accounts: List<UserAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthorProfileIfAbsent(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAuthorProfilesIfAbsent(profiles: List<UserProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComment(entity: PostCommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertComments(entities: List<PostCommentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReply(entity: PostReplyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReplies(entities: List<PostReplyEntity>)

    @Query("SELECT * FROM post_cache WHERE postId = :postId LIMIT 1")
    suspend fun getPostEntity(postId: String): PostEntity?

    @Query(
        """
        SELECT
            post.postId AS postId,
            post.content AS content,
            post.imagesJson AS imagesJson,
            post.createTime AS createTime,
            COALESCE(user_post.userId, 0) AS authorId,
            COALESCE(user_profile.displayName, user_account.accountName, '') AS authorName,
            COALESCE(user_profile.avatarUrl, '') AS authorAvatarUrl
        FROM post_cache AS post
        LEFT JOIN user_post ON user_post.postId = post.postId
        LEFT JOIN user_account ON user_account.userId = user_post.userId
        LEFT JOIN user_profile ON user_profile.userId = user_post.userId
        WHERE post.postId = :postId
        LIMIT 1
        """
    )
    fun getPostWithAuthorFlow(postId: String): Flow<PostWithAuthorEntity?>

    @Query(
        """
        SELECT
            post.postId AS postId,
            post.content AS content,
            post.imagesJson AS imagesJson,
            post.createTime AS createTime,
            COALESCE(user_post.userId, 0) AS authorId,
            COALESCE(user_profile.displayName, user_account.accountName, '') AS authorName,
            COALESCE(user_profile.avatarUrl, '') AS authorAvatarUrl
        FROM post_cache AS post
        LEFT JOIN user_post ON user_post.postId = post.postId
        LEFT JOIN user_account ON user_account.userId = user_post.userId
        LEFT JOIN user_profile ON user_profile.userId = user_post.userId
        ORDER BY post.createTime DESC
        """
    )
    fun getAllPostWithAuthorsFlow(): Flow<List<PostWithAuthorEntity>>

    @Query(
        """
        SELECT
            post.postId AS postId,
            post.content AS content,
            post.imagesJson AS imagesJson,
            post.createTime AS createTime,
            COALESCE(user_post.userId, 0) AS authorId,
            COALESCE(user_profile.displayName, user_account.accountName, '') AS authorName,
            COALESCE(user_profile.avatarUrl, '') AS authorAvatarUrl
        FROM post_cache AS post
        INNER JOIN user_post ON user_post.postId = post.postId
        LEFT JOIN user_account ON user_account.userId = user_post.userId
        LEFT JOIN user_profile ON user_profile.userId = user_post.userId
        WHERE user_post.userId = :userId
        ORDER BY post.createTime DESC
        """
    )
    fun getPostsByUserId(userId: Long): Flow<List<PostWithAuthorEntity>>

    @Query(
        """
        SELECT
            post.postId AS postId,
            post.content AS content,
            post.imagesJson AS imagesJson,
            post.createTime AS createTime,
            COALESCE(user_post.userId, 0) AS authorId,
            COALESCE(user_profile.displayName, user_account.accountName, '') AS authorName,
            COALESCE(user_profile.avatarUrl, '') AS authorAvatarUrl
        FROM user_liked_post
        INNER JOIN post_cache AS post ON post.postId = user_liked_post.postId
        LEFT JOIN user_post ON user_post.postId = post.postId
        LEFT JOIN user_account ON user_account.userId = user_post.userId
        LEFT JOIN user_profile ON user_profile.userId = user_post.userId
        WHERE user_liked_post.userId = :userId
        ORDER BY user_liked_post.likedAt DESC
        """
    )
    fun getLikedPostsByUserId(userId: Long): Flow<List<PostWithAuthorEntity>>

    @Query("SELECT * FROM post_comment WHERE postId = :postId ORDER BY time DESC")
    fun getCommentEntitiesFlow(postId: String): Flow<List<PostCommentEntity>>

    @Query("SELECT * FROM post_comment WHERE postId = :postId ORDER BY time DESC")
    suspend fun getCommentEntities(postId: String): List<PostCommentEntity>

    @Query("SELECT * FROM post_comment WHERE postId = :postId ORDER BY time DESC LIMIT :limit OFFSET :offset")
    suspend fun getCommentEntitiesPage(postId: String, limit: Int, offset: Int): List<PostCommentEntity>

    @Query("SELECT * FROM post_reply WHERE postId = :postId AND parentCommentId IN (:commentIds) ORDER BY time ASC")
    suspend fun getReplyEntitiesByCommentIds(postId: String, commentIds: List<Long>): List<PostReplyEntity>

    @Query("DELETE FROM post_reply WHERE postId = :postId")
    suspend fun deleteRepliesByPostId(postId: String)

    @Query("DELETE FROM post_comment WHERE postId = :postId")
    suspend fun deleteCommentsByPostId(postId: String)

    @Query("DELETE FROM post_cache WHERE postId = :postId")
    suspend fun deleteById(postId: String)

    @Transaction
    suspend fun replacePostComments(
        postId: String,
        comments: List<PostCommentEntity>,
        replies: List<PostReplyEntity>
    ) {
        deleteRepliesByPostId(postId)
        deleteCommentsByPostId(postId)
        upsertComments(comments)
        upsertReplies(replies)
    }
}
