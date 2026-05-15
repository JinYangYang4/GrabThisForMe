package com.example.grabthisforme.model.store.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "store_cache",
    indices = [
        Index(value = ["ownerId"]),
        Index(value = ["type"]),
        Index(value = ["isOpen"])
    ]
)
data class StoreEntity(
    @PrimaryKey val storeId: Long,
    val ownerId: Long = 0L,
    val name: String = "",
    val type: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val businessHours: String? = null,
    val minOrderAmount: String = "0",
    val deliveryFee: String = "0",
    val isOpen: Boolean = true,
    val pic: String? = null,
    val rating: Float = 0.0f,
    val tags: String = "",
    val goodsGroupsJson: String = "",
    val salesVolume: Long = 0L
)
