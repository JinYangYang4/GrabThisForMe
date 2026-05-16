package com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.domain.StoreGoodsGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreOwnerViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _showStorePage = MutableLiveData(false)
    private val _storeNameText = MutableLiveData("")
    private val _storeSaleCountText = MutableLiveData("")
    private val _storeAddressText = MutableLiveData("")
    private val _storeServiceText = MutableLiveData("")
    private val _storeNoticeText = MutableLiveData("")
    private val _storeDeliveryText = MutableLiveData("")
    private val _storeBusinessHoursText = MutableLiveData("")
    private val selectedStoreId = MutableStateFlow<Long?>(null)

    val showStorePage: LiveData<Boolean> get() = _showStorePage
    val storeNameText: LiveData<String> get() = _storeNameText
    val storeSaleCountText: LiveData<String> get() = _storeSaleCountText
    val storeAddressText: LiveData<String> get() = _storeAddressText
    val storeServiceText: LiveData<String> get() = _storeServiceText
    val storeNoticeText: LiveData<String> get() = _storeNoticeText
    val storeDeliveryText: LiveData<String> get() = _storeDeliveryText
    val storeBusinessHoursText: LiveData<String> get() = _storeBusinessHoursText

    val currentStore: StateFlow<Store?> = selectedStoreId
        .flatMapLatest { storeId ->
            if (storeId == null || storeId <= 0L) {
                flowOf(null)
            } else {
                storeRepository.getStoreFlow(storeId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val goodsList: StateFlow<List<Goods>> = currentStore
        .map { it?.goodsAll.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            currentStore.collectLatest { store ->
                updateStoreDetails(store)
            }
        }
    }

    val categoryList: StateFlow<List<String>> = currentStore
        .map { store ->
            val categories = store?.category.orEmpty()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
            if (categories.isEmpty()) listOf("全部") else categories
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf("全部")
        )

    fun loadStore(storeId: Long) {
        selectedStoreId.value = storeId.takeIf { it > 0L }
    }

    fun setShowStorePage(showStore: Boolean) {
        _showStorePage.value = showStore
    }

    fun addGoodsToCategory(
        store: Store,
        category: String,
        goods: Goods
    ): Store {
        val normalizedCategory = category.trim()
        if (normalizedCategory.isBlank()) {
            return store
        }

        val groups = store.goodsGroups.toMutableList()
        val index = groups.indexOfFirst { it.category == normalizedCategory }

        if (index >= 0) {
            val targetGroup = groups[index]
            val goodsList = targetGroup.goods.toMutableList()
            val goodsIndex = goodsList.indexOfFirst { it.id == goods.id }
            if (goodsIndex >= 0) {
                goodsList[goodsIndex] = goods
            } else {
                goodsList.add(goods)
            }
            groups[index] = targetGroup.copy(goods = goodsList)
        } else {
            groups.add(
                StoreGoodsGroup(
                    category = normalizedCategory,
                    goods = listOf(goods)
                )
            )
        }

        return store.copy(goodsGroups = groups)
    }

    private fun updateStoreDetails(store: Store?) {
        _storeNameText.value = store?.name.orEmpty()
        _storeSaleCountText.value = store?.salesVolume?.takeIf { it > 0L }?.let { "已售：${it}+" }.orEmpty()
        _storeAddressText.value = store?.address.orEmpty()
        _storeServiceText.value = store?.phone?.trim()?.takeIf { it.isNotEmpty() }?.let { "联系电话：$it" }.orEmpty()
        _storeNoticeText.value = store?.tags
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { "店铺标签：" + it.joinToString("、") }
            .orEmpty()
        _storeDeliveryText.value = store?.let { buildDeliveryText(it) }.orEmpty()
        _storeBusinessHoursText.value = store?.businessHours?.trim()?.takeIf { it.isNotEmpty() }?.let { "营业时间：$it" }.orEmpty()
    }

    private fun buildDeliveryText(store: Store): String {
        val parts = mutableListOf<String>()
        if (store.minOrderAmount > java.math.BigDecimal.ZERO) {
            parts.add("起送 " + store.minOrderAmount.stripTrailingZeros().toPlainString())
        }
        if (store.deliveryFee > java.math.BigDecimal.ZERO) {
            parts.add("配送费 " + store.deliveryFee.stripTrailingZeros().toPlainString())
        }
        return if (parts.isNotEmpty()) {
            "配送信息：" + parts.joinToString("，")
        } else {
            ""
        }
    }
}
