package com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchFriendOrGroupOrConversationViewModel(private val searchDao: SearchDao) : ViewModel() {
    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()

    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList

    private val _isExpanded = MutableLiveData<Boolean>(false)
    val isExpanded: LiveData<Boolean> = _isExpanded

    var fullList: MutableList<SearchContent> = mutableListOf()
    var limitedList: MutableList<SearchContent> = mutableListOf()

    fun refreshLimitedList(){
        limitedList = if (_isExpanded.value == true){
            fullList
        }else{
            if (fullList.size > 10) {
                fullList.take(10).toMutableList()
            } else {
                fullList.toMutableList()
            }
        }
        _searchHistoryList.postValue(limitedList)
    }
    fun loadSearchHistory() {
        viewModelScope.launch {
            fullList = searchDao.getSearchByType(SearchContent.SearchType.FRIEND).first().toMutableList()
            limitedList = if (_isExpanded.value == true){
                 fullList
            }else{
                if (fullList.size > 10) {
                    fullList.take(10).toMutableList()
                } else {
                    fullList.toMutableList()
                }
            }
            _searchHistoryList.postValue(limitedList)
        }
    }
    fun setExpand(isExpand : Boolean){
        _isExpanded.value = isExpand
    }

    fun addSearchHistory(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            val newItem = SearchContent(
                search_time = currentTime,
                content = content,
                searchType = SearchContent.SearchType.FRIEND
            )

            searchDao.deleteByTypeAndContent(SearchContent.SearchType.FRIEND, content)
            searchDao.insertSearchContent(newItem)

            fullList.removeIf { it.content == content }
            fullList.add(0, newItem)
            limitedList = if (_isExpanded.value == true){
                fullList
            }else{
                if (fullList.size > 10) {
                    fullList.take(10).toMutableList()
                } else {
                    fullList
                }
            }
            _searchHistoryList.postValue(limitedList)
        }
    }


    fun deleteByContent(content: String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.FRIEND, content)

            fullList.removeIf { it.content == content }
            limitedList = if (_isExpanded.value == true){
                fullList
            }else{
                if (fullList.size > 10) {
                    fullList.take(10).toMutableList()
                } else {
                    fullList
                }
            }
            _searchHistoryList.postValue(limitedList)
        }
    }

    fun deleteHistory(content: String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.FRIEND, content)
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            searchDao.clearByType(SearchContent.SearchType.FRIEND)
            fullList.clear()
        }
        _searchHistoryList.postValue(mutableListOf())
    }
}