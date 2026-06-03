package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import androidx.fragment.app.Fragment
import com.example.grabthisforme.ui.goods.adapter.CategoryPagerAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.view.FragmentRecyclerViewGoods
import com.example.grabthisforme.model.goods.domain.Goods

class GoodsViewPager2Adapter(
    fragment: Fragment,
    private val categories: List<Goods.GoodsCategory>
) : CategoryPagerAdapter<Goods.GoodsCategory>(
    fragment = fragment,
    items = categories,
    fragmentFactory = { FragmentRecyclerViewGoods.newInstance(it) }
)
