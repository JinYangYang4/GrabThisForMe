package com.example.grabthisforme.model.store.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.store.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StoreEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<StoreEntity>)

    @Query("SELECT * FROM store_cache WHERE storeId = :storeId LIMIT 1")
    fun getStoreEntityFlow(storeId: Long): Flow<StoreEntity?>

    @Query("SELECT * FROM store_cache ORDER BY storeId DESC")
    fun getAllStoreEntitiesFlow(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM store_cache WHERE ownerId = :ownerId ORDER BY storeId DESC")
    fun getStoreEntitiesByOwnerFlow(ownerId: Long): Flow<List<StoreEntity>>

    @Query("DELETE FROM store_cache WHERE storeId = :storeId")
    suspend fun deleteById(storeId: Long)
}
