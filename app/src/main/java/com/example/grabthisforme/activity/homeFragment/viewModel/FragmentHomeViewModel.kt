package com.example.grabthisforme.activity.homeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.homeFragment.ui_model.HomeStoreCardUiModel
import com.example.grabthisforme.activity.homeFragment.ui_model.HomeStorePreviewItemUiModel
import com.example.grabthisforme.activity.homeFragment.ui_model.OrderListItemUiModel
import com.example.grabthisforme.activity.homeFragment.ui_model.createHomeStoreMoreEntryUiModel
import com.example.grabthisforme.activity.homeFragment.ui_model.toHomeStorePreviewItemUiModel
import com.example.grabthisforme.activity.homeFragment.ui_model.toOrderListItemUiModel
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FragmentHomeViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    goodsRepository: GoodsRepository,
    storeRepository: StoreRepository
) : ViewModel() {
    private var isAnimating = false
    private var alreadyShow = false
    private var rvTaskHeight = 0
    private var _rvTaskIsOpen = MutableLiveData<Boolean>(false)
    val rvTaskIsOpen : LiveData<Boolean> get() = _rvTaskIsOpen
    private val _dropdownVisible = MutableLiveData(false)
    val dropdownVisible: LiveData<Boolean> get() = _dropdownVisible

    private val _outerRvAtBottom = MutableLiveData(false)
    val  outerRvAtBottom : LiveData<Boolean> get() = _outerRvAtBottom

    val currentTaskOrders: StateFlow<List<OrderListItemUiModel>> = orderRepository.currentOrderList
        .map { orders -> orders.map { it.toOrderListItemUiModel() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    val homeStoreCards: StateFlow<List<HomeStoreCardUiModel>> = combine(
        storeRepository.allStoreList,
        goodsRepository.allGoodsList
    ) { stores, goodsList ->
        val goodsByStoreId = goodsList.groupBy { it.storeId }
        stores.map { store ->
            val previewGoods = goodsByStoreId[store.id]
                .orEmpty()
                .let(::buildPreviewGoods)
            HomeStoreCardUiModel(
                storeId = store.id,
                storeName = store.name.ifBlank { "校园店铺" },
                salesText = "已售 ${store.salesVolume}",
                distanceText = "约1公里",
                storeImageUrl = store.pic,
                previewGoods = previewGoods
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun setOuterRvAtBottom(atBottom : Boolean){
        _outerRvAtBottom.value = atBottom
    }

    fun setRvTaskHeight(height : Int){
        rvTaskHeight = height
    }
    fun getRvTaskHeight(): Int {
        return rvTaskHeight
    }
    fun MakeIsAnimatingTrue(){
        isAnimating = true
    }
    fun MakeIsAnimatingFalse(){
        isAnimating = false
    }
    fun GetIsAnimating(): Boolean {
        return isAnimating
    }


    fun MakeAlreadyShowTure(){
        alreadyShow = true
        _dropdownVisible.value = true
    }
    fun MakeAlreadyShowFalse(){
        alreadyShow = false
    }
    fun GetAlreadyShow(): Boolean {
        return alreadyShow
    }


    fun MakeRvTaskIsOpenTure(){
        _rvTaskIsOpen.value = true

    }
    fun MakeRvTaskIsOpenFalse(){
        _rvTaskIsOpen.value = false
    }
    fun GetRvTaskIsOpen(): Boolean {
        return _rvTaskIsOpen.value == true
    }

    private fun buildPreviewGoods(goodsList: List<Goods>): List<HomeStorePreviewItemUiModel> {
        val previewGoods = if (goodsList.size > 15) {
            goodsList.take(14)
        } else {
            goodsList
        }
        return previewGoods.map { it.toHomeStorePreviewItemUiModel() } + createHomeStoreMoreEntryUiModel()
    }
}
