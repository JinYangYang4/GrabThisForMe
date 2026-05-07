package com.example.grabthisforme.model.store.domain

import java.math.BigDecimal

data class StoreIdentity(
    val id: Long,
    val name: String,
    val type: String,
    val ownerId: Long = 0L
)

data class StoreLocation(
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class StoreCommercialInfo(
    val phone: String? = null,
    val businessHours: String? = null,
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val isOpen: Boolean = true,
    val pic: String? = null,
    val rating: Float = 0.0f,
    val tags: List<String> = emptyList()
)

data class StoreStatistics(
    val salesVolume: Long = 0
)
