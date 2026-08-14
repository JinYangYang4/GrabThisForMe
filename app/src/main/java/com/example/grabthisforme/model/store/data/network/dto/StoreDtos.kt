package com.example.grabthisforme.model.store.data.network.dto

import com.example.grabthisforme.model.goods.data.network.dto.GoodsDto
import java.math.BigDecimal

data class StoreIdentityDto(
    val id: Long,
    val name: String = "",
    val type: String = "",
    val ownerId: Long = 0L
)

data class StoreLocationDto(
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class StoreCommercialInfoDto(
    val phone: String? = null,
    val businessHours: String? = null,
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val isOpen: Boolean = true,
    val pic: String? = null,
    val rating: Float = 0.0f,
    val tags: List<String> = emptyList()
)

data class StoreStatisticsDto(
    val salesVolume: Long = 0
)

data class StoreGoodsCategoryDto(
    val groupId: Long,
    val category: String = "",
    val sortOrder: Int = 0,
    val goods: List<GoodsDto> = emptyList()
)

data class StoreDto(
    val id: Long,
    val ownerId: Long = 0L,
    val name: String = "",
    val type: String = "",
    val address: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val businessHours: String? = null,
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val isOpen: Boolean = true,
    val pic: String? = null,
    val rating: Float = 0.0f,
    val salesVolume: Long = 0L,
    val tags: List<String> = emptyList(),
    val categories: List<StoreGoodsCategoryDto> = emptyList()
)
