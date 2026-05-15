package com.example.grabthisforme.activity.fragment_misc.create.model

import com.example.grabthisforme.model.goods.domain.Goods

data class CreateGoodsRegistration(
    val name: String,
    val description: String,
    val categoryText: String,
    val priceText: String,
    val discountPriceText: String,
    val tagText: String,
    val stockText: String,
    val imageUrl: String
) {
    data class Parsed(
        val name: String,
        val description: String,
        val category: Goods.GoodsCategory,
        val price: Double,
        val discountPrice: Double,
        val tag: String,
        val stock: Int,
        val imageUrl: String
    )

    fun parseOrError(): Result<Parsed> {
        val normalizedName = name.trim()
        val normalizedDescription = description.trim()
        val normalizedCategoryText = categoryText.trim()
        val normalizedTag = tagText.trim()
        val normalizedImageUrl = imageUrl.trim()

        if (normalizedName.isBlank()) {
            return Result.failure(IllegalArgumentException("请输入商品名称"))
        }
        if (normalizedDescription.isBlank()) {
            return Result.failure(IllegalArgumentException("请输入商品描述"))
        }
        if (normalizedCategoryText.isBlank()) {
            return Result.failure(IllegalArgumentException("请输入商品分类"))
        }

        val price = priceText.trim().toDoubleOrNull()
            ?: return Result.failure(IllegalArgumentException("价格格式不正确"))
        if (price <= 0.0) {
            return Result.failure(IllegalArgumentException("价格必须大于0"))
        }

        val discountPrice = discountPriceText.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
            ?: 0.0
        if (discountPrice < 0.0) {
            return Result.failure(IllegalArgumentException("折扣价不能小于0"))
        }
        if (discountPrice > price) {
            return Result.failure(IllegalArgumentException("折扣价不能高于原价"))
        }

        val stock = stockText.trim().takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
        if (stock < 0) {
            return Result.failure(IllegalArgumentException("库存不能小于0"))
        }

        val category = mapCategory(normalizedCategoryText)

        return Result.success(
            Parsed(
                name = normalizedName,
                description = normalizedDescription,
                category = category,
                price = price,
                discountPrice = discountPrice,
                tag = normalizedTag,
                stock = stock,
                imageUrl = normalizedImageUrl
            )
        )
    }

    private fun mapCategory(text: String): Goods.GoodsCategory {
        return Goods.GoodsCategory.entries.firstOrNull {
            it.name.equals(text, ignoreCase = true) || it.desc == text
        } ?: Goods.GoodsCategory.OTHER
    }
}
