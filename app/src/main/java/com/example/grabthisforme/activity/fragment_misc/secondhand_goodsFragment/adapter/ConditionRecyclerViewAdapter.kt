package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import com.example.grabthisforme.ui.goods.adapter.FilterChipRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.model.ConditionModel

class ConditionRecyclerViewAdapter(
    clickListener: (condition: ConditionModel) -> Unit
) : FilterChipRecyclerViewAdapter<ConditionModel>(
    idProvider = { model -> model.id },
    labelProvider = { model -> model.conditionText },
    selectedProvider = { model -> model.isSelected },
    clickListener = clickListener
)
