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
    private val _openMySelectGoosView = MutableLiveData(false)
    private val _showStorePage = MutableLiveData(false)
    private val _deleteMode = MutableLiveData(false)
    private val _historyEmpty = MutableLiveData(true)
    private val _searchInput = MutableLiveData("")
    private val selectedStoreId = MutableStateFlow<Long?>(null)

    val priceTotal: LiveData<Double> get() = _priceTotal
    val isOpenMySelectGoosView: LiveData<Boolean> get() = _openMySelectGoosView
    val showStorePage: LiveData<Boolean> get() = _showStorePage
    val deleteMode: LiveData<Boolean> get() = _deleteMode
    val historyEmpty: LiveData<Boolean> get() = _historyEmpty
    val searchInput: LiveData<String> get() = _searchInput

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


    fun loadStore(storeId: Long) {
        selectedStoreId.value = storeId.takeIf { it > 0L }
    }

    fun addGoods(price: Double) {
        _priceTotal.value = (_priceTotal.value ?: 0.0) + price
    }

    fun minusGoods() {}

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
}
