package com.example.grabthisforme.model.store.data.dto

import com.example.grabthisforme.model.goods.data.dto.GoodsDto
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
    val category: String = "",
    val goods: List<GoodsDto> = emptyList()
)

data class StoreDto(
    val identity: StoreIdentityDto,
    val location: StoreLocationDto = StoreLocationDto(),
    val commercialInfo: StoreCommercialInfoDto = StoreCommercialInfoDto(),
    val statistics: StoreStatisticsDto = StoreStatisticsDto()
)
