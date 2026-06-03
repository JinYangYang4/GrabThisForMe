package com.example.grabthisforme.activity.fragment_misc.goodsFragment.adapter

import androidx.fragment.app.Fragment
import com.example.grabthisforme.ui.goods.adapter.CategoryPagerAdapter
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsMarketplaceSection
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.view.FragmentMarketplaceGoodsList

class GoodsMarketplacePagerAdapter(
    fragment: Fragment,
    sections: List<GoodsMarketplaceSection>
) : CategoryPagerAdapter<GoodsMarketplaceSection>(
    fragment = fragment,
    items = sections,
    fragmentFactory = { FragmentMarketplaceGoodsList.newInstance(it) }
)
