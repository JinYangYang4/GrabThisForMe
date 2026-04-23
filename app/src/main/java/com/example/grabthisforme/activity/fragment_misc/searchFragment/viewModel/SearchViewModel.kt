package com.example.grabthisforme.activity.fragment_misc.searchFragment.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchDao: SearchDao
) : ViewModel() {
    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()
    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList
    val _selectedSearchContent = MutableLiveData<SearchContent?>()
    val selectedSearchContent : LiveData<SearchContent?> get() = _selectedSearchContent
    var fullList : MutableList<SearchContent> = mutableListOf()
    private val _deleteMode = MutableLiveData(false)
    val deleteMode: LiveData<Boolean> get() = _deleteMode
    private val _historyEmpty = MutableLiveData(true)
    val historyEmpty: LiveData<Boolean> get() = _historyEmpty
    private val _searchInput = MutableLiveData("")
    val searchInput: LiveData<String> get() = _searchInput

    fun loadSearchHistory() {
        viewModelScope.launch {
            fullList = searchDao.getSearchByType(SearchContent.SearchType.SHOPPING).first().toMutableList()
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
            val newItem = SearchContent(search_time = currentTime, content = content)
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.SHOPPING,content)
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

    fun deleteByContent(content: String){
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.SHOPPING,content)
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

    fun deleteHistory(content : String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.SHOPPING,content)
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            searchDao. clearByType(SearchContent.SearchType.SHOPPING)
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