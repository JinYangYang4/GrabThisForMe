package com.example.grabthisforme.activity.fragment_misc.goods_detail.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.goods_detail.model.GoodsDetailUiState
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GoodsDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    goodsRepository: GoodsRepository,
    storeRepository: StoreRepository
) : ViewModel() {

    private val targetGoodsId = savedStateHandle.get<Long>("goodsId") ?: -1L

    val uiState: StateFlow<GoodsDetailUiState> = combine(
        goodsRepository.allGoodsList,
        goodsRepository.secondhandGoodsList,
        storeRepository.allStoreList
    ) { goodsList, secondhandGoodsList, storeList ->
        val fallbackGoods = goodsList.firstOrNull() ?: goodsRepository.getSingleDisplayGoods()
        val goods = goodsList.firstOrNull { it.id == targetGoodsId } ?: fallbackGoods
        val secondhandGoods = secondhandGoodsList.firstOrNull { it.id == goods.id }
        val store = resolveStore(goods, storeList)

        GoodsDetailUiState(
            goods = goods,
            store = store,
            secondhandGoods = secondhandGoods,
            isFallbackData = targetGoodsId <= 0L || goods.id != targetGoodsId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoodsDetailUiState(goods = Goods.getSingleVirtualGoods(), isFallbackData = true)
    )

    private fun resolveStore(goods: Goods, storeList: List<Store>): Store? {
        return storeList.firstOrNull { it.id == goods.storeId }
    }
}
