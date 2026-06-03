package com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.StoreGoodsListItemUiModel
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.StoreOwnerGoodsItemUiModel
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.toStoreGoodsListItemUiModel
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.toStoreOwnerGoodsItemUiModel
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.domain.usecase.AssignGoodsToStoreCategoryUseCase
import com.example.grabthisforme.model.store.domain.usecase.MoveGoodsToStoreUnclassifiedUseCase
import com.example.grabthisforme.model.store.domain.usecase.UpdateStoreCategoriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StoreOwnerViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val updateStoreCategoriesUseCase: UpdateStoreCategoriesUseCase,
    private val assignGoodsToStoreCategoryUseCase: AssignGoodsToStoreCategoryUseCase,
    private val moveGoodsToStoreUnclassifiedUseCase: MoveGoodsToStoreUnclassifiedUseCase
) : ViewModel() {
    private val _showStorePage = MutableLiveData(false)
    private val _storeNameText = MutableLiveData("")
    private val _storeSaleCountText = MutableLiveData("")
    private val _storeAddressText = MutableLiveData("")
    private val _storeServiceText = MutableLiveData("")
    private val _storeNoticeText = MutableLiveData("")
    private val _storeDeliveryText = MutableLiveData("")
    private val _storeBusinessHoursText = MutableLiveData("")
    private val _openUnselectGoodsView = MutableLiveData(false)
    private val selectedCategory = MutableStateFlow(Store.CATEGORY_ALL)
    private val selectedStoreId = MutableStateFlow<Long?>(null)

    val showStorePage: LiveData<Boolean> get() = _showStorePage
    val storeNameText: LiveData<String> get() = _storeNameText
    val storeSaleCountText: LiveData<String> get() = _storeSaleCountText
    val storeAddressText: LiveData<String> get() = _storeAddressText
    val storeServiceText: LiveData<String> get() = _storeServiceText
    val storeNoticeText: LiveData<String> get() = _storeNoticeText
    val storeDeliveryText: LiveData<String> get() = _storeDeliveryText
    val storeBusinessHoursText: LiveData<String> get() = _storeBusinessHoursText
    val isOpenUnselectGoodsView: LiveData<Boolean> get() = _openUnselectGoodsView
    val currentSelectedCategory: StateFlow<String> = selectedCategory

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

    val categoryList: StateFlow<List<String>> = selectedStoreId
        .flatMapLatest { storeId ->
            if (storeId != null && storeId > 0L) {
                storeRepository.observeStoreCategories(storeId)
            } else {
                flowOf(listOf(Store.CATEGORY_ALL, Store.CATEGORY_UNCLASSIFIED))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf(Store.CATEGORY_ALL, Store.CATEGORY_UNCLASSIFIED)
        )

    val goodsList: StateFlow<List<StoreOwnerGoodsItemUiModel>> = combine(
        selectedStoreId,
        selectedCategory
    ) { storeId, category ->
        storeId to category
    }.flatMapLatest { (storeId, category) ->
        loadGoodsByStoreAndCategory(storeId, category)
    }.map { goods ->
        goods.map { it.toStoreOwnerGoodsItemUiModel() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val unselectGoodsList: StateFlow<List<StoreGoodsListItemUiModel>> = combine(
        selectedStoreId,
        selectedCategory
    ) { storeId, category ->
        storeId to category
    }.flatMapLatest { (storeId, category) ->
        loadUnselectGoods(storeId, category)
    }.map { goods ->
        goods.map { it.toStoreGoodsListItemUiModel() }
    }.stateIn(
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
        viewModelScope.launch {
            categoryList.collectLatest {
                syncSelectedCategory()
            }
        }
    }

    private fun loadGoodsByStoreAndCategory(
        storeId: Long?,
        category: String
    ) = if (storeId != null && storeId > 0L) {
        storeRepository.observeGoodsByStoreAndCategory(storeId, category)
    } else {
        flowOf(emptyList())
    }

    private fun loadUnselectGoods(
        storeId: Long?,
        category: String
    ) = if (storeId != null && storeId > 0L) {
        storeRepository.observeGoodsOutsideSelectedCategory(storeId, category)
    } else {
        flowOf(emptyList())
    }

    fun loadStore(storeId: Long) {
        selectedStoreId.value = storeId.takeIf { it > 0L }
        selectedCategory.value = Store.CATEGORY_ALL
    }

    fun setShowStorePage(showStore: Boolean) {
        _showStorePage.value = showStore
        if (showStore) {
            _openUnselectGoodsView.value = false
        }
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category.trim().ifBlank { Store.CATEGORY_ALL }
        _openUnselectGoodsView.value = false
    }

    fun setOpenUnselectGoodsView(isOpen: Boolean) {
        _openUnselectGoodsView.value = isOpen
    }

    fun tryOpenUnselectGoodsView() {
        _openUnselectGoodsView.value = isActionableCategory(selectedCategory.value)
    }

    fun updateStore(store: Store) {
        viewModelScope.launch {
            runCatching {
                storeRepository.updateStore(store)
            }
        }
    }

    fun updateStoreCategories(
        categories: List<String>,
        renamedCategories: Map<String, String> = emptyMap()
    ) {
        val storeId = currentStore.value?.id ?: return
        viewModelScope.launch {
            updateStoreCategoriesUseCase(
                storeId = storeId,
                categories = categories,
                renamedCategories = renamedCategories
            )
        }
    }

    fun addGoodsToCurrentCategory(goodsId: Long) {
        val storeId = currentStore.value?.id ?: return
        val category = selectedCategory.value
        if (!isActionableCategory(category)) return

        viewModelScope.launch {
            if (category == Store.CATEGORY_UNCLASSIFIED) {
                moveGoodsToStoreUnclassifiedUseCase(
                    storeId = storeId,
                    goodsId = goodsId
                )
            } else {
                assignGoodsToStoreCategoryUseCase(
                    storeId = storeId,
                    goodsId = goodsId,
                    category = category
                )
            }
        }
    }

    private fun updateStoreDetails(store: Store?) {
        _storeNameText.value = store?.name.orEmpty()
        _storeSaleCountText.value = store?.salesVolume
            ?.takeIf { it > 0L }
            ?.let { "已售：${it}+" }
            .orEmpty()
        _storeAddressText.value = store?.address.orEmpty()
        _storeServiceText.value = store?.phone
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { "联系电话：$it" }
            .orEmpty()
        _storeNoticeText.value = store?.tags
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { "店铺标签：${it.joinToString("、")}" }
            .orEmpty()
        _storeDeliveryText.value = store?.let(::buildDeliveryText).orEmpty()
        _storeBusinessHoursText.value = store?.businessHours
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { "营业时间：$it" }
            .orEmpty()
    }

    private fun buildDeliveryText(store: Store): String {
        val parts = mutableListOf<String>()
        if (store.minOrderAmount > BigDecimal.ZERO) {
            parts.add("起送 ${store.minOrderAmount.stripTrailingZeros().toPlainString()} 元")
        }
        if (store.deliveryFee > BigDecimal.ZERO) {
            parts.add("配送费 ${store.deliveryFee.stripTrailingZeros().toPlainString()} 元")
        }
        return if (parts.isNotEmpty()) {
            "配送信息：${parts.joinToString("，")}"
        } else {
            ""
        }
    }

    private fun syncSelectedCategory() {
        val availableCategories = categoryList.value
        if (selectedCategory.value !in availableCategories) {
            selectedCategory.value = Store.CATEGORY_ALL
            _openUnselectGoodsView.value = false
        }
    }

    private fun isActionableCategory(category: String): Boolean {
        return category.isNotBlank() && category != Store.CATEGORY_ALL
    }
}
