package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.view.FragmentRecyclerViewGoods
import com.example.grabthisforme.model.goods.domain.Goods

class GoodsViewPager2Adapter(
    fragment: Fragment,
    private val categories: List<Goods.GoodsCategory>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = categories.size

    override fun createFragment(position: Int): Fragment {
        return FragmentRecyclerViewGoods.newInstance(categories[position])
    }
}
