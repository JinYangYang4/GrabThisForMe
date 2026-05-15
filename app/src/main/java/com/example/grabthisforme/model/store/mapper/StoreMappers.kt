package com.example.grabthisforme.model.store.mapper

import android.util.Log
import com.example.grabthisforme.model.goods.data.dto.GoodsBaseDto
import com.example.grabthisforme.model.goods.data.dto.GoodsDto
import com.example.grabthisforme.model.goods.data.dto.GoodsPriceDto
import com.example.grabthisforme.model.goods.data.dto.GoodsStateDto
import com.example.grabthisforme.model.goods.data.dto.GoodsUiDto
import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDto
import com.example.grabthisforme.model.store.data.dto.StoreCommercialInfoDto
import com.example.grabthisforme.model.store.data.dto.StoreDto
import com.example.grabthisforme.model.store.data.dto.StoreGoodsGroupDto
import com.example.grabthisforme.model.store.data.dto.StoreIdentityDto
import com.example.grabthisforme.model.store.data.dto.StoreLocationDto
import com.example.grabthisforme.model.store.data.dto.StoreStatisticsDto
import com.example.grabthisforme.model.store.data.entity.StoreEntity
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.domain.StoreCommercialInfo
import com.example.grabthisforme.model.store.domain.StoreGoodsGroup
import com.example.grabthisforme.model.store.domain.StoreIdentity
import com.example.grabthisforme.model.store.domain.StoreLocation
import com.example.grabthisforme.model.store.domain.StoreStatistics
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

private const val STORE_TAG_SEPARATOR = "|||"
private const val KEY_GOODS_GROUP_CATEGORY = "category"
private const val KEY_GOODS_GROUP_GOODS = "goods"
private const val KEY_GOODS_BASE = "base"
private const val KEY_GOODS_PRICE = "price"
private const val KEY_GOODS_UI = "ui"
private const val KEY_GOODS_STATE = "state"

private fun String.toBigDecimalOrZero(): BigDecimal {
    if (isBlank()) {
        return BigDecimal.ZERO
    }
    return runCatching { BigDecimal(this) }.getOrDefault(BigDecimal.ZERO)
}

private fun List<String>.toTagStorage(): String {
    return joinToString(separator = STORE_TAG_SEPARATOR)
}

private fun String.toTagList(): List<String> {
    if (isBlank()) {
        return emptyList()
    }
    return split(STORE_TAG_SEPARATOR).filter { it.isNotBlank() }
}

private fun GoodsDto.toJsonObject(): JSONObject {
    return JSONObject().apply {
        put(
            KEY_GOODS_BASE,
            JSONObject().apply {
                put("id", base.id)
                put("storeId", base.storeId)
                put("name", base.name)
                put("message", base.message)
                put("category", base.category)
            }
        )
        put(
            KEY_GOODS_PRICE,
            JSONObject().apply {
                put("price", price.price)
                put("discountPrice", price.discountPrice)
                put("discountTag", price.discountTag)
            }
        )
        put(
            KEY_GOODS_UI,
            JSONObject().apply {
                put("pic", ui.pic)
                put("tag", ui.tag)
                put("unit", ui.unit)
                put("selectedCount", ui.selectedCount)
            }
        )
        put(
            KEY_GOODS_STATE,
            JSONObject().apply {
                put("saleNumber", state.saleNumber)
                put("stock", state.stock)
                put("isSoldOut", state.isSoldOut)
                put("isHot", state.isHot)
                put("purchaseStatus", state.purchaseStatus)
                put("soldCount", state.soldCount)
            }
        )
    }
}

private fun JSONObject.toGoodsDto(): GoodsDto {
    val base = optJSONObject(KEY_GOODS_BASE) ?: JSONObject()
    val price = optJSONObject(KEY_GOODS_PRICE) ?: JSONObject()
    val ui = optJSONObject(KEY_GOODS_UI) ?: JSONObject()
    val state = optJSONObject(KEY_GOODS_STATE) ?: JSONObject()

    return GoodsDto(
        base = GoodsBaseDto(
            id = base.optLong("id", 0L),
            storeId = base.optLong("storeId", 0L),
            name = base.optString("name"),
            message = base.optString("message"),
            category = base.optString("category").ifBlank { null }
        ),
        price = GoodsPriceDto(
            price = price.optDouble("price", 0.0),
            discountPrice = price.optDouble("discountPrice", 0.0),
            discountTag = price.optString("discountTag")
        ),
        ui = GoodsUiDto(
            pic = ui.optString("pic"),
            tag = ui.optString("tag"),
            unit = ui.optString("unit"),
            selectedCount = ui.optInt("selectedCount", 0)
        ),
        state = GoodsStateDto(
            saleNumber = state.optLong("saleNumber", 0L),
            stock = state.optInt("stock", 0),
            isSoldOut = state.optBoolean("isSoldOut", false),
            isHot = state.optBoolean("isHot", false),
            purchaseStatus = state.optInt("purchaseStatus", 0),
            soldCount = state.optLong("soldCount", 0L)
        )
    )
}

private fun List<StoreGoodsGroup>.toGoodsGroupsJson(): String {
    if (isEmpty()) return ""
    val jsonArray = JSONArray()
    forEach { group ->
        val goodsJsonArray = JSONArray()
        group.goods.forEach { goods ->
            goodsJsonArray.put(goods.toDto().toJsonObject())
        }
        jsonArray.put(
            JSONObject().apply {
                put(KEY_GOODS_GROUP_CATEGORY, group.category)
                put(KEY_GOODS_GROUP_GOODS, goodsJsonArray)
            }
        )
    }
    return jsonArray.toString()
}

private fun String.toGoodsGroups(): List<StoreGoodsGroup> {
    if (isBlank()) return emptyList()
    return runCatching {
        val jsonArray = JSONArray(this)
        Log.d("test11", "toGoodsGroups: $jsonArray")
        List(jsonArray.length()) { index ->
            val modelObject = jsonArray.optJSONObject(index) ?: JSONObject()
            val category = modelObject.optString(KEY_GOODS_GROUP_CATEGORY)
            val goodsArray = modelObject.optJSONArray(KEY_GOODS_GROUP_GOODS) ?: JSONArray()
            val goodsList = List(goodsArray.length()) { goodsIndex ->
                val goodsObject = goodsArray.optJSONObject(goodsIndex) ?: JSONObject()
                goodsObject.toGoodsDto().toDomain()
            }
            StoreGoodsGroup(
                category = category,
                goods = goodsList
            )
        }
    }.getOrDefault(emptyList())
}

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

private fun StoreGoodsGroupDto.toDomainInfo(): StoreGoodsGroup {
    val normalizedCategory = category.trim()
    val goodsInCategory = goods.map { it.toDomain() }
    return StoreGoodsGroup(
        category = normalizedCategory,
        goods = goodsInCategory
    )
}

private fun StoreGoodsGroup.toDtoInfo(): StoreGoodsGroupDto {
    return StoreGoodsGroupDto(
        category = category,
        goods = goods.map { it.toDto() }
    )
}

fun StoreDto.toDomain(): Store {
    val modelsFromDto = goodsGroups
        ?.map { it.toDomainInfo() }
        ?.takeIf { it.isNotEmpty() }

    val legacyGoods = goodsAll?.map { it.toDomain() } //遗留商品
    val resolvedModels = modelsFromDto ?: Store.composeGoodsGroups(legacyGoods)

    return Store(
        identity = identity.toDomainInfo(),
        location = location.toDomainInfo(),
        commercialInfo = commercialInfo.toDomainInfo(),
        statistics = statistics.toDomainInfo(),
        goodsGroups = resolvedModels
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
        goodsGroups = goodsGroups.map { it.toDtoInfo() },
        goodsAll = goodsAll.map { it.toDto() }
    )
}

fun StoreEntity.toDomain(): Store {
    return Store(
        identity = StoreIdentity(
            id = storeId,
            name = name,
            type = type,
            ownerId = ownerId
        ),
        location = StoreLocation(
            address = address,
            latitude = latitude,
            longitude = longitude
        ),
        commercialInfo = StoreCommercialInfo(
            phone = phone,
            businessHours = businessHours,
            minOrderAmount = minOrderAmount.toBigDecimalOrZero(),
            deliveryFee = deliveryFee.toBigDecimalOrZero(),
            isOpen = isOpen,
            pic = pic,
            rating = rating,
            tags = tags.toTagList()
        ),
        statistics = StoreStatistics(
            salesVolume = salesVolume
        ),
        goodsGroups = goodsGroupsJson.toGoodsGroups()
    )
}

fun Store.toEntity(): StoreEntity {
    return StoreEntity(
        storeId = id,
        ownerId = ownerId,
        name = name,
        type = type,
        address = address,
        latitude = latitude,
        longitude = longitude,
        phone = phone,
        businessHours = businessHours,
        minOrderAmount = minOrderAmount.toPlainString(),
        deliveryFee = deliveryFee.toPlainString(),
        isOpen = isOpen,
        pic = pic,
        rating = rating,
        tags = tags.toTagStorage(),
        goodsGroupsJson = goodsGroups.toGoodsGroupsJson(),
        salesVolume = salesVolume
    )
}
