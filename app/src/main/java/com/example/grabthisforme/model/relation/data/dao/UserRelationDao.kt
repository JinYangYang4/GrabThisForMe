package com.example.grabthisforme.model.relation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.relation.data.entity.UserLikedGoodsEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedPostEntity
import com.example.grabthisforme.model.relation.data.entity.UserLikedStoreEntity
import com.example.grabthisforme.model.relation.data.entity.UserPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRelationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserPost(entity: UserPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserPosts(entities: List<UserPostEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLikedPost(entity: UserLikedPostEntity)

    @Query("SELECT * FROM user_liked_post WHERE userId = :userId ORDER BY likedAt DESC")
    suspend fun getLikedPostsByUserId(userId: Long): List<UserLikedPostEntity>

    @Query("DELETE FROM user_liked_post WHERE userId = :userId AND postId = :postId")
    suspend fun deleteLikedPost(userId: Long, postId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_liked_post WHERE userId = :userId AND postId = :postId)")
    fun isPostLikedFlow(userId: Long, postId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM user_liked_post WHERE userId = :userId AND postId = :postId)")
    suspend fun isPostLiked(userId: Long, postId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLikedStore(entity: UserLikedStoreEntity)

    @Query("SELECT * FROM user_liked_store WHERE userId = :userId ORDER BY likedAt DESC")
    suspend fun getLikedStoresByUserId(userId: Long): List<UserLikedStoreEntity>

    @Query("DELETE FROM user_liked_store WHERE userId = :userId AND storeId = :storeId")
    suspend fun deleteLikedStore(userId: Long, storeId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM user_liked_store WHERE userId = :userId AND storeId = :storeId)")
    fun isStoreLikedFlow(userId: Long, storeId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM user_liked_store WHERE userId = :userId AND storeId = :storeId)")
    suspend fun isStoreLiked(userId: Long, storeId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLikedGoods(entity: UserLikedGoodsEntity)

    @Query("SELECT * FROM user_liked_goods WHERE userId = :userId ORDER BY likedAt DESC")
    suspend fun getLikedGoodsByUserId(userId: Long): List<UserLikedGoodsEntity>

    @Query("DELETE FROM user_liked_goods WHERE userId = :userId AND goodsId = :goodsId")
    suspend fun deleteLikedGoods(userId: Long, goodsId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM user_liked_goods WHERE userId = :userId AND goodsId = :goodsId)")
    fun isGoodsLikedFlow(userId: Long, goodsId: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM user_liked_goods WHERE userId = :userId AND goodsId = :goodsId)")
    suspend fun isGoodsLiked(userId: Long, goodsId: Long): Boolean

    @Query("SELECT * FROM user_post WHERE userId = :userId ORDER BY postId DESC")
    suspend fun getUserPostsByUserId(userId: Long): List<UserPostEntity>
}
