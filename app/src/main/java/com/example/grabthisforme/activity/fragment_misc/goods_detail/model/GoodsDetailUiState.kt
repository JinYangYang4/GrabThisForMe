package com.example.grabthisforme.activity.fragment_misc.goods_detail.model

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.store.domain.Store

data class GoodsDetailUiState(
    val goods: Goods,
    val store: Store? = null,
    val secondhandGoods: SecondhandGoods? = null,
    val isFallbackData: Boolean = false
)
