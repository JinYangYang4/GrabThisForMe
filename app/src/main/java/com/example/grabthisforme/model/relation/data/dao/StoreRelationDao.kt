package com.example.grabthisforme.model.relation.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryEntity
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryItemEntity
import com.example.grabthisforme.model.relation.data.entity.StoreTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreRelationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(entity: StoreGoodsCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(entities: List<StoreGoodsCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryItem(entity: StoreGoodsCategoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryItems(entities: List<StoreGoodsCategoryItemEntity>)

    @Query("DELETE FROM store_goods_group WHERE storeId = :storeId")
    suspend fun deleteCategoriesByStoreId(storeId: Long)

    @Query("DELETE FROM store_goods_group_item WHERE groupId = :groupId")
    suspend fun deleteCategoryItemsByCategoryId(groupId: Long)

    @Query("DELETE FROM store_goods_group_item WHERE groupId IN (SELECT groupId FROM store_goods_group WHERE storeId = :storeId)")
    suspend fun deleteAllCategoryItemsByStoreId(storeId: Long)

    @Query("SELECT * FROM store_goods_group WHERE storeId = :storeId ORDER BY sortOrder ASC")
    suspend fun getCategoriesByStoreId(storeId: Long): List<StoreGoodsCategoryEntity>

    @Query("SELECT * FROM store_goods_group_item WHERE groupId = :groupId ORDER BY sortOrder ASC")
    suspend fun getCategoryItemsByCategoryId(groupId: Long): List<StoreGoodsCategoryItemEntity>

    @Query("SELECT * FROM store_goods_group_item WHERE groupId IN (SELECT groupId FROM store_goods_group WHERE storeId = :storeId) ORDER BY groupId ASC, sortOrder ASC")
    suspend fun getCategoryItemsByStoreId(storeId: Long): List<StoreGoodsCategoryItemEntity>

    @Query("SELECT * FROM store_goods_group ORDER BY storeId ASC, sortOrder ASC")
    fun observeAllCategories(): Flow<List<StoreGoodsCategoryEntity>>

    @Query("SELECT * FROM store_goods_group WHERE storeId = :storeId ORDER BY sortOrder ASC")
    fun observeCategoriesByStoreId(storeId: Long): Flow<List<StoreGoodsCategoryEntity>>

    @Query("SELECT * FROM store_goods_group_item ORDER BY groupId ASC, sortOrder ASC")
    fun observeAllCategoryItems(): Flow<List<StoreGoodsCategoryItemEntity>>

    @Query("SELECT * FROM store_goods_group_item WHERE groupId IN (SELECT groupId FROM store_goods_group WHERE storeId = :storeId) ORDER BY groupId ASC, sortOrder ASC")
    fun observeCategoryItemsByStoreId(storeId: Long): Flow<List<StoreGoodsCategoryItemEntity>>

    @Transaction
    suspend fun replaceGoodsCategories(
        storeId: Long,
        categories: List<StoreGoodsCategoryEntity>,
        items: List<StoreGoodsCategoryItemEntity>
    ) {
        deleteAllCategoryItemsByStoreId(storeId)
        deleteCategoriesByStoreId(storeId)
        if (categories.isNotEmpty()) {
            insertCategories(categories)
        }
        if (items.isNotEmpty()) {
            insertCategoryItems(items)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(entity: StoreTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(entities: List<StoreTagEntity>)

    @Query("DELETE FROM store_tag WHERE storeId = :storeId")
    suspend fun deleteTagsByStoreId(storeId: Long)

    @Query("SELECT * FROM store_tag ORDER BY storeId ASC, sortOrder ASC")
    fun observeAllTags(): Flow<List<StoreTagEntity>>

    @Query("SELECT * FROM store_tag WHERE storeId = :storeId ORDER BY sortOrder ASC")
    fun observeTagsByStoreId(storeId: Long): Flow<List<StoreTagEntity>>

    @Transaction
    suspend fun replaceTags(storeId: Long, tags: List<StoreTagEntity>) {
        deleteTagsByStoreId(storeId)
        if (tags.isNotEmpty()) {
            insertTags(tags)
        }
    }
}
