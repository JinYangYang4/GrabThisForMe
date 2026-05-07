package com.example.grabthisforme.model.store.mapper

import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDto
import com.example.grabthisforme.model.store.data.dto.StoreCommercialInfoDto
import com.example.grabthisforme.model.store.data.dto.StoreDto
import com.example.grabthisforme.model.store.data.dto.StoreIdentityDto
import com.example.grabthisforme.model.store.data.dto.StoreLocationDto
import com.example.grabthisforme.model.store.data.dto.StoreStatisticsDto
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.domain.StoreCommercialInfo
import com.example.grabthisforme.model.store.domain.StoreIdentity
import com.example.grabthisforme.model.store.domain.StoreLocation
import com.example.grabthisforme.model.store.domain.StoreStatistics

fun StoreIdentityDto.toDomainInfo(): StoreIdentity {
    return StoreIdentity(
        id = id,
        name = name,
        type = type,
        ownerId = ownerId
    )
}

fun StoreLocationDto.toDomainInfo(): StoreLocation {
    return StoreLocation(
        address = address,
        latitude = latitude,
        longitude = longitude
    )
}

fun StoreCommercialInfoDto.toDomainInfo(): StoreCommercialInfo {
    return StoreCommercialInfo(
        phone = phone,
        businessHours = businessHours,
        minOrderAmount = minOrderAmount,
        deliveryFee = deliveryFee,
        isOpen = isOpen,
        pic = pic,
        rating = rating,
        tags = tags
    )
}

fun StoreStatisticsDto.toDomainInfo(): StoreStatistics {
    return StoreStatistics(
        salesVolume = salesVolume
    )
}

fun StoreDto.toDomain(): Store {
    return Store(
        identity = identity.toDomainInfo(),
        location = location.toDomainInfo(),
        commercialInfo = commercialInfo.toDomainInfo(),
        statistics = statistics.toDomainInfo(),
        goodsAll = goodsAll?.map { it.toDomain() }
    )
}

fun Store.toDto(): StoreDto {
    return StoreDto(
        identity = StoreIdentityDto(
            id = id,
            name = name,
            type = type,
            ownerId = ownerId
        ),
        location = StoreLocationDto(
            address = address,
            latitude = latitude,
            longitude = longitude
        ),
        commercialInfo = StoreCommercialInfoDto(
            phone = phone,
            businessHours = businessHours,
            minOrderAmount = minOrderAmount,
            deliveryFee = deliveryFee,
            isOpen = isOpen,
            pic = pic,
            rating = rating,
            tags = tags
        ),
        statistics = StoreStatisticsDto(
            salesVolume = salesVolume
        ),
        goodsAll = goodsAll?.map { it.toDto() }
    )
}
