package com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchDao
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.SelectedGoodsItemUiModel
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.StoreGoodsListItemUiModel
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.toSelectedGoodsItemUiModel
import com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model.toStoreGoodsListItemUiModel
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.order.data.network.api.PurchaseItemRequest
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import com.example.grabthisforme.model.coupon.data.network.dto.UserCouponDto
import com.example.grabthisforme.model.coupon.data.repository.CouponRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val searchDao: SearchDao,
    private val storeRepository: StoreRepository,
    private val orderRepository: OrderRepository,
    private val couponRepository: CouponRepository
) : ViewModel() {
    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()
    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList

    private val _selectedSearchContent = MutableLiveData<SearchContent?>()
    val selectedSearchContent: LiveData<SearchContent?> get() = _selectedSearchContent

    var fullList: MutableList<SearchContent> = mutableListOf()
    private val _priceTotal = MutableLiveData(0.0)
    private val _priceTotalText = MutableLiveData("¥0.00")
    private val _currentAlreadySelectList = MutableLiveData<List<SelectedGoodsItemUiModel>>(emptyList())
    private val _openMySelectGoosView = MutableLiveData(false)
    private val _showStorePage = MutableLiveData(false)
    private val _deleteMode = MutableLiveData(false)
    private val _historyEmpty = MutableLiveData(true)
    private val _searchInput = MutableLiveData("")
    private val _storeNameText = MutableLiveData("")
    private val _storeSaleCountText = MutableLiveData("")
    private val _storeAddressText = MutableLiveData("")
    private val _storeServiceText = MutableLiveData("")
    private val _storeNoticeText = MutableLiveData("")
    private val _storeDeliveryText = MutableLiveData("")
    private val _storeBusinessHoursText = MutableLiveData("")
    private val _purchaseResult = MutableLiveData<PurchaseUiResult>()
    private val _purchaseInProgress = MutableLiveData(false)
    private val _couponSelection = MutableLiveData<CouponSelectionEvent>()
    private val selectedCategory = MutableStateFlow(Store.CATEGORY_ALL)
    private val selectedStoreId = MutableStateFlow<Long?>(null)

    val priceTotal: LiveData<Double> get() = _priceTotal
    val priceTotalText: LiveData<String> get() = _priceTotalText
    val currentAlreadySelectList: LiveData<List<SelectedGoodsItemUiModel>> get() = _currentAlreadySelectList
    val isOpenMySelectGoosView: LiveData<Boolean> get() = _openMySelectGoosView
    val showStorePage: LiveData<Boolean> get() = _showStorePage
    val deleteMode: LiveData<Boolean> get() = _deleteMode
    val historyEmpty: LiveData<Boolean> get() = _historyEmpty
    val searchInput: LiveData<String> get() = _searchInput
    val storeNameText: LiveData<String> get() = _storeNameText
    val storeSaleCountText: LiveData<String> get() = _storeSaleCountText
    val storeAddressText: LiveData<String> get() = _storeAddressText
    val storeServiceText: LiveData<String> get() = _storeServiceText
    val storeNoticeText: LiveData<String> get() = _storeNoticeText
    val storeDeliveryText: LiveData<String> get() = _storeDeliveryText
    val storeBusinessHoursText: LiveData<String> get() = _storeBusinessHoursText
    val purchaseResult: LiveData<PurchaseUiResult> get() = _purchaseResult
    val purchaseInProgress: LiveData<Boolean> get() = _purchaseInProgress
    val couponSelection: LiveData<CouponSelectionEvent> get() = _couponSelection
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

    val storeCategories: StateFlow<List<String>> = selectedStoreId
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

    val goodsList: StateFlow<List<StoreGoodsListItemUiModel>> = combine(
        selectedStoreId,
        selectedCategory
    ) { storeId, category ->
        storeId to category
    }.flatMapLatest { (storeId, category) ->
        loadGoodsByStoreAndCategory(storeId, category)
    }.map { goods ->
        goods.map { it.toStoreGoodsListItemUiModel() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    private fun loadGoodsByStoreAndCategory(
        storeId: Long?,
        category: String
    ): Flow<List<Goods>> {
        if (storeId == null || storeId <= 0L) return flowOf(emptyList())
        return storeRepository.observeGoodsByStoreAndCategory(storeId, category)
    }

    init {
        viewModelScope.launch {
            currentStore.collectLatest { store ->
                updateStoreDetails(store)
            }
        }
        viewModelScope.launch {
            storeCategories.collectLatest {
                syncSelectedCategory()
            }
        }
    }

    fun loadStore(storeId: Long) {
        selectedStoreId.value = storeId.takeIf { it > 0L }
        selectedCategory.value = Store.CATEGORY_ALL
        if (storeId > 0L) {
            viewModelScope.launch { runCatching { storeRepository.refreshStore(storeId) } }
        }
    }

    fun selectCategory(category: String) {
        selectedCategory.value = category.trim().ifBlank { Store.CATEGORY_ALL }
    }

    fun addGoods(goods: StoreGoodsListItemUiModel) {
        val currentList = _currentAlreadySelectList.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.goodsId == goods.goodsId }
        if (index >= 0) {
            val target = currentList[index]
            currentList[index] = target.copy(selectedCount = target.selectedCount + 1)
        } else {
            currentList.add(goods.toSelectedGoodsItemUiModel())
        }
        updateSelectedGoods(currentList)
    }

    fun increaseSelectedGoods(item: SelectedGoodsItemUiModel) {
        val currentList = _currentAlreadySelectList.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.goodsId == item.goodsId }
        if (index < 0) return
        val target = currentList[index]
        currentList[index] = target.copy(selectedCount = target.selectedCount + 1)
        updateSelectedGoods(currentList)
    }

    fun decreaseSelectedGoods(item: SelectedGoodsItemUiModel) {
        val currentList = _currentAlreadySelectList.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.goodsId == item.goodsId }
        if (index < 0) return
        val target = currentList[index]
        if (target.selectedCount > 1) {
            currentList[index] = target.copy(selectedCount = target.selectedCount - 1)
        } else {
            currentList.removeAt(index)
        }
        updateSelectedGoods(currentList)
    }

    fun clearSelectedGoods() {
        _currentAlreadySelectList.value = emptyList()
        _priceTotal.value = 0.0
        _priceTotalText.value = "¥0.00"
    }

    fun prepareCheckout() {
        if (_purchaseInProgress.value == true) return
        val selected = _currentAlreadySelectList.value.orEmpty()
        val storeId = selectedStoreId.value
        if (selected.isEmpty() || storeId == null) {
            _purchaseResult.value = PurchaseUiResult(false, "请先选择商品")
            return
        }
        _purchaseInProgress.value = true
        viewModelScope.launch {
            runCatching { couponRepository.listApplicable(storeId, _priceTotal.value ?: 0.0) }
                .onSuccess { _couponSelection.postValue(CouponSelectionEvent(it)) }
                .onFailure {
                    _purchaseResult.postValue(PurchaseUiResult(false, it.message ?: "加载可用优惠券失败"))
                }
            _purchaseInProgress.postValue(false)
        }
    }

    fun checkoutSelectedGoods(userCouponId: String? = null) {
        if (_purchaseInProgress.value == true) return
        val selected = _currentAlreadySelectList.value.orEmpty()
        if (selected.isEmpty()) {
            _purchaseResult.value = PurchaseUiResult(false, "请先选择商品")
            return
        }
        _purchaseInProgress.value = true
        viewModelScope.launch {
            runCatching {
                orderRepository.purchase(
                    clientPurchaseId = UUID.randomUUID().toString(),
                    userCouponId = userCouponId,
                    items = selected.map { PurchaseItemRequest(it.goodsId, it.selectedCount) }
                )
            }.onSuccess { result ->
                clearSelectedGoods()
                selectedStoreId.value?.let { storeId ->
                    runCatching { storeRepository.refreshStore(storeId) }
                }
                _purchaseResult.postValue(
                    PurchaseUiResult(
                        success = true,
                        message = if (result.discountAmount > 0) {
                            "购买成功，优惠 ¥${String.format(Locale.getDefault(), "%.2f", result.discountAmount)}，实付 ¥${String.format(Locale.getDefault(), "%.2f", result.totalAmount)}"
                        } else {
                            "购买成功，实付 ¥${String.format(Locale.getDefault(), "%.2f", result.totalAmount)}"
                        },
                        purchaseId = result.purchaseId
                    )
                )
            }.onFailure { error ->
                _purchaseResult.postValue(
                    PurchaseUiResult(false, error.message ?: "购买失败，请稍后重试")
                )
            }
            _purchaseInProgress.postValue(false)
        }
    }

    fun setMySelectGoosView(isOpen: Boolean) {
        _openMySelectGoosView.value = isOpen
    }

    fun setShowStorePage(showStore: Boolean) {
        _showStorePage.value = showStore
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            fullList = searchDao.getSearchByType(SearchContent.SearchType.STORE).first().toMutableList()
            val limitedList = if (fullList.size > 10) {
                fullList.take(10).toMutableList()
            } else {
                fullList.toMutableList()
            }
            _searchHistoryList.postValue(limitedList)
            _historyEmpty.postValue(limitedList.isEmpty())
        }
    }

    fun addSearchHistory(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val newItem = SearchContent(
                search_time = currentTime,
                content = content,
                searchType = SearchContent.SearchType.STORE
            )
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.STORE, content)
            searchDao.insertSearchContent(newItem)
            fullList.removeIf { it.content == content }
            fullList.add(0, newItem)
            val limitedList = if (fullList.size > 10) {
                fullList.take(10).toMutableList()
            } else {
                fullList
            }
            _searchHistoryList.postValue(limitedList)
            _historyEmpty.postValue(limitedList.isEmpty())
        }
    }

    fun deleteByContent(content: String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.STORE, content)
            fullList.removeIf { it.content == content }
            val limitedList = if (fullList.size > 10) {
                fullList.take(10).toMutableList()
            } else {
                fullList
            }
            _searchHistoryList.postValue(limitedList)
            _historyEmpty.postValue(limitedList.isEmpty())
        }
    }

    fun onSearchItemClick(searchContent: SearchContent) {
        _selectedSearchContent.value = searchContent
    }

    fun deleteHistory(content: String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.STORE, content)
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            searchDao.clearByType(SearchContent.SearchType.STORE)
            fullList.clear()
        }
        _searchHistoryList.postValue(mutableListOf())
        _historyEmpty.postValue(true)
    }

    fun setDeleteMode(enabled: Boolean) {
        _deleteMode.value = enabled
    }

    fun updateSearchInput(content: String) {
        _searchInput.value = content
    }

    fun clearSearchInput() {
        _searchInput.value = ""
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

    private fun updateSelectedGoods(selectedList: List<SelectedGoodsItemUiModel>) {
        val totalPrice = roundToPriceScale(
            selectedList.sumOf { it.price * it.selectedCount }
        )
        _currentAlreadySelectList.value = selectedList
        _priceTotal.value = totalPrice
        _priceTotalText.value = String.format(Locale.getDefault(), "¥%.2f", totalPrice)
    }

    private fun roundToPriceScale(value: Double): Double {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
    }

    private fun syncSelectedCategory() {
        val availableCategories = storeCategories.value
        if (selectedCategory.value !in availableCategories) {
            selectedCategory.value = Store.CATEGORY_ALL
        }
    }
}

data class PurchaseUiResult(
    val success: Boolean,
    val message: String,
    val purchaseId: String? = null,
    val eventId: Long = System.currentTimeMillis()
)

data class CouponSelectionEvent(
    val coupons: List<UserCouponDto>,
    val eventId: Long = System.nanoTime()
)
