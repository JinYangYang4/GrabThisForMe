package com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val searchDao: SearchDao
): ViewModel() {
    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()
    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList

    private val _selectedSearchContent = MutableLiveData<SearchContent?>()
    val selectedSearchContent: LiveData<SearchContent?> get() = _selectedSearchContent

    var fullList: MutableList<SearchContent> = mutableListOf()
    private var _priceTotal =  MutableLiveData<Double>(0.0)
    private var _openMySelectGoosView = MutableLiveData<Boolean>(false)
    private val _showStorePage = MutableLiveData(false)
    private val _deleteMode = MutableLiveData(false)
    private val _historyEmpty = MutableLiveData(true)
    private val _searchInput = MutableLiveData("")

    val priceTotal : LiveData<Double> get() = _priceTotal
    val isOpenMySelectGoosView : LiveData<Boolean> get() = _openMySelectGoosView
    val showStorePage: LiveData<Boolean> get() = _showStorePage
    val deleteMode: LiveData<Boolean> get() = _deleteMode
    val historyEmpty: LiveData<Boolean> get() = _historyEmpty
    val searchInput: LiveData<String> get() = _searchInput
    fun addGoods(price : Double){
        _priceTotal.value = _priceTotal.value + price
    }
    fun minusGoods(){}
    fun setMySelectGoosView(open : Boolean){
        if (open == true){
            _openMySelectGoosView.value = true
        }else{
            _openMySelectGoosView.value = false
        }
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