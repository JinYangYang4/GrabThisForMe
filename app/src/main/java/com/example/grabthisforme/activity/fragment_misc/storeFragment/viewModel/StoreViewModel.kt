package com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val searchDao: SearchDao,
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()
    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList

    private val _selectedSearchContent = MutableLiveData<SearchContent?>()
    val selectedSearchContent: LiveData<SearchContent?> get() = _selectedSearchContent

    var fullList: MutableList<SearchContent> = mutableListOf()
    private val _priceTotal = MutableLiveData(0.0)
    private val _currentAlreadySelectList = MutableLiveData<MutableList<Goods>>(mutableListOf())
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
    private val selectedStoreId = MutableStateFlow<Long?>(null)

    val priceTotal: LiveData<Double> get() = _priceTotal
    val currentAlreadySelectList: LiveData<MutableList<Goods>> get() = _currentAlreadySelectList
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


    fun loadStore(storeId: Long) {
        selectedStoreId.value = storeId.takeIf { it > 0L }
    }

    fun addGoods(goods: Goods) {
        val currentList = _currentAlreadySelectList.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == goods.id }
        if (index >= 0) {
            val target = currentList[index]
            target.selectedCount += 1
            currentList[index] = target
        } else {
            currentList.add(goods.withSelectedCount(1))
        }
        _currentAlreadySelectList.value = currentList
        recalculatePriceTotal(currentList)
    }

    fun increaseSelectedGoods(goods: Goods) {
        val currentList = _currentAlreadySelectList.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == goods.id }
        if (index < 0) return
        val target = currentList[index]
        target.selectedCount += 1
        currentList[index] = target
        _currentAlreadySelectList.value = currentList
        recalculatePriceTotal(currentList)
    }

    fun decreaseSelectedGoods(goods: Goods) {
        val currentList = _currentAlreadySelectList.value?.toMutableList() ?: mutableListOf()
        val index = currentList.indexOfFirst { it.id == goods.id }
        if (index < 0) return
        val target = currentList[index]
        if (target.selectedCount > 1) {
            target.selectedCount -= 1
            currentList[index] = target
        } else {
            currentList.removeAt(index)
        }
        _currentAlreadySelectList.value = currentList
        recalculatePriceTotal(currentList)
    }

    fun clearSelectedGoods() {
        _currentAlreadySelectList.value = mutableListOf()
        _priceTotal.value = 0.0
    }

    fun setMySelectGoosView(open: Boolean) {
        _openMySelectGoosView.value = open
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

    private fun recalculatePriceTotal(selectedList: List<Goods>) {
        _priceTotal.value = selectedList.sumOf { it.price * it.selectedCount }
    }
}
