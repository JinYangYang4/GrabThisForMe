package com.example.grabthisforme.activity.fragment_misc.goodsFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsFilterChip
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsFilterType
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsMarketplaceSection
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.ui_model.GoodsMarketplaceItemUiModel
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.ui_model.toGoodsMarketplaceItemUiModel
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoodsMarketplaceViewModel @Inject constructor(
    val goodsRepository: GoodsRepository
) : ViewModel() {

    private val _filterChips = MutableLiveData<List<GoodsFilterChip>>()
    val filterChips: LiveData<List<GoodsFilterChip>> = _filterChips

    private val selectedSection = MutableStateFlow(GoodsMarketplaceSection.ALL)
    private val selectedFilterType = MutableStateFlow(GoodsFilterType.ALL)

    private val _filteredGoodsList = MutableLiveData<List<GoodsMarketplaceItemUiModel>?>(null)
    val filteredGoodsList: LiveData<List<GoodsMarketplaceItemUiModel>?> = _filteredGoodsList

    init {
        initFilterChips()
        viewModelScope.launch {
            combine(
                goodsRepository.allGoodsList,
                selectedSection,
                selectedFilterType
            ) { goodsList, section, filterType ->
                goodsList
                    .filter { goods ->
                        matchesSection(goods, section) && matchesFilter(goods, filterType)
                    }
                    .map { it.toGoodsMarketplaceItemUiModel() }
            }.collect { goods ->
                _filteredGoodsList.value = goods
            }
        }
    }

    fun setSection(section: GoodsMarketplaceSection) {
        selectedSection.value = section
        if (_filterChips.value.isNullOrEmpty()) {
            initFilterChips()
        }
    }

    fun selectFilter(targetId: Long) {
        val currentList = _filterChips.value.orEmpty()
        val newList = currentList.map { chip ->
            chip.copy(isSelected = chip.id == targetId)
        }
        val selected = newList.firstOrNull { it.id == targetId } ?: return
        _filterChips.value = newList
        selectedFilterType.value = selected.type
    }

    private fun initFilterChips() {
        val chips = listOf(
            GoodsFilterChip(0L, "全部", GoodsFilterType.ALL, isSelected = true),
            GoodsFilterChip(1L, "便利快取", GoodsFilterType.READY),
            GoodsFilterChip(2L, "折扣优先", GoodsFilterType.DISCOUNT),
            GoodsFilterChip(3L, "热卖高频", GoodsFilterType.HOT),
            GoodsFilterChip(4L, "校园店铺", GoodsFilterType.STORE_TYPE)
        )
        _filterChips.value = chips
        selectedFilterType.value = GoodsFilterType.ALL
    }

    private fun matchesSection(goods: Goods, section: GoodsMarketplaceSection): Boolean {
        return when (section) {
            GoodsMarketplaceSection.ALL -> true
            GoodsMarketplaceSection.READY_TO_EAT ->
                goods.category == Goods.GoodsCategory.FOOD || goods.tag.contains("到店取")
            GoodsMarketplaceSection.STUDY_SUPPLY ->
                goods.category == Goods.GoodsCategory.BOOK ||
                    goods.tag.contains("打印店") ||
                    goods.tag.contains("文具店")
            GoodsMarketplaceSection.DORM_LIFE ->
                goods.category == Goods.GoodsCategory.HOME || goods.category == Goods.GoodsCategory.DIGITAL
            GoodsMarketplaceSection.SPORT_FUN ->
                goods.category == Goods.GoodsCategory.SPORT || goods.category == Goods.GoodsCategory.CLOTHING
        }
    }

    private fun matchesFilter(goods: Goods, filterType: GoodsFilterType): Boolean {
        return when (filterType) {
            GoodsFilterType.ALL -> true
            GoodsFilterType.STORE_TYPE -> goods.storeId > 0L
            GoodsFilterType.DISCOUNT -> goods.discountPrice > 0 && goods.discountPrice < goods.price
            GoodsFilterType.HOT -> goods.isHot || goods.soldCount >= 50
            GoodsFilterType.READY ->
                goods.category == Goods.GoodsCategory.FOOD ||
                    goods.tag.contains("现做") ||
                    goods.tag.contains("到店取")
        }
    }
}
