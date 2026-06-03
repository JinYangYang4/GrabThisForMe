package com.example.grabthisforme.activity.fragment_misc.goodsFragment.adapter

import com.example.grabthisforme.ui.goods.adapter.FilterChipRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsFilterChip

class GoodsMarketplaceFilterAdapter(
    clickListener: (GoodsFilterChip) -> Unit
) : FilterChipRecyclerViewAdapter<GoodsFilterChip>(
    idProvider = { it.id },
    labelProvider = { it.label },
    selectedProvider = { it.isSelected },
    clickListener = clickListener
)
