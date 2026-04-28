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

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<OrderEntity>)

    @Query("SELECT * FROM order_cache WHERE orderId = :orderId LIMIT 1")
    suspend fun getOrderEntity(orderId: String): OrderEntity?

    @Query("SELECT * FROM order_cache ORDER BY startTime DESC")
    suspend fun getAllOrderEntities(): List<OrderEntity>

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

    suspend fun getOrder(orderId: String): Order? {
        return getOrderEntity(orderId)?.toDomain()
    }

    suspend fun getAllOrders(): List<Order> {
        return getAllOrderEntities().map { it.toDomain() }
    }
}
