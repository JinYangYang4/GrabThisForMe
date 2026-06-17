package com.example.grabthisforme.model.order.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.grabthisforme.model.order.data.local.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<OrderEntity>)

    @Query("SELECT * FROM order_cache WHERE orderId = :orderId LIMIT 1")
    fun observeOrderEntity(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM order_cache ORDER BY startTime DESC")
    fun observeAllOrderEntities(): Flow<List<OrderEntity>>

    @Query("DELETE FROM order_cache WHERE orderId = :orderId")
    suspend fun deleteById(orderId: String)

    @Query("SELECT COUNT(*) FROM order_cache")
    suspend fun getOrderCount(): Int
}
