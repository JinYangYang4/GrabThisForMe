package com.example.grabthisforme.model.store.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.store.data.entity.StoreEntity
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.mapper.toDomain
import com.example.grabthisforme.model.store.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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

    @Transaction
    suspend fun saveStore(store: Store) {
        upsert(store.toEntity())
    }

    @Transaction
    suspend fun saveStores(stores: List<Store>) {
        upsertAll(stores.map { it.toEntity() })
    }

    fun getStore(storeId: Long): Flow<Store?> {
        return getStoreEntityFlow(storeId).map { it?.toDomain() }
    }

    fun getAllStores(): Flow<List<Store>> {
        return getAllStoreEntitiesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getStoresByOwner(ownerId: Long): Flow<List<Store>> {
        return getStoreEntitiesByOwnerFlow(ownerId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
