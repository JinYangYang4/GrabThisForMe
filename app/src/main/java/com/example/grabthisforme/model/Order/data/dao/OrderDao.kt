package com.example.grabthisforme.model.order.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.order.data.entity.OrderEntity
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.mapper.toDomain
import com.example.grabthisforme.model.order.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<OrderEntity>)

    @Query("SELECT * FROM order_cache WHERE orderId = :orderId LIMIT 1")
    fun getOrderEntityFlow(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM order_cache ORDER BY startTime DESC")
    fun getAllOrderEntitiesFlow(): Flow<List<OrderEntity>>

    @Query("DELETE FROM order_cache WHERE orderId = :orderId")
    suspend fun deleteById(orderId: String)

    @Transaction
    suspend fun saveOrder(order: Order) {
        upsert(order.toEntity())
    }

    @Transaction
    suspend fun saveOrders(orders: List<Order>) {
        upsertAll(orders.map { it.toEntity() })
    }

    fun getOrder(orderId: String): Flow<Order?> {
        return getOrderEntityFlow(orderId).map { it?.toDomain() }
    }

    fun getAllOrders(): Flow<List<Order>> {
        return getAllOrderEntitiesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
