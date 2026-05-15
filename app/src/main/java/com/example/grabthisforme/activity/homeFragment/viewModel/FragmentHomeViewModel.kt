package com.example.grabthisforme.activity.homeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
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

    val currentTaskOrders: StateFlow<List<Order>> = orderRepository.currentOrderList
    val allGoods: StateFlow<List<Goods>> = goodsRepository.allGoodsList
    val allStores: StateFlow<List<Store>> = storeRepository.allStoreList

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
}
