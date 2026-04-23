package com.example.grabthisforme.activity.fragment_misc.searchCommunityFragment.viewModle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchCommunityViewModel(private val searchDao: SearchDao) : ViewModel() {
    private val _searchHistoryList = MutableLiveData<MutableList<SearchContent>>()
    val searchHistoryList: LiveData<MutableList<SearchContent>> get() = _searchHistoryList

    private var _searchRecomment =  MutableLiveData<List<SearchContent>>()
    val searchRecomment : LiveData<List<SearchContent>> get() = _searchRecomment

    var fullList : MutableList<SearchContent> = mutableListOf()
    private val _deleteMode = MutableLiveData(false)
    val deleteMode: LiveData<Boolean> get() = _deleteMode
    private val _historyEmpty = MutableLiveData(true)
    val historyEmpty: LiveData<Boolean> get() = _historyEmpty
    private val _searchInput = MutableLiveData("")
    val searchInput: LiveData<String> get() = _searchInput
    private val _recommendVisible = MutableLiveData(true)
    val recommendVisible: LiveData<Boolean> get() = _recommendVisible

    fun initSearchRecomment(){
        _searchRecomment.value = SearchContent.SearchRecommendations.getGuessYouSearch().subList(0,12)
    }

    fun loadSearchHistory() {
        viewModelScope.launch {
            fullList = searchDao.getSearchByType(SearchContent.SearchType.COMMUNITY).first().toMutableList()
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
                searchType = SearchContent.SearchType.COMMUNITY
            )
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.COMMUNITY, content)
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
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.COMMUNITY, content)
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

    fun deleteHistory(content : String) {
        viewModelScope.launch {
            searchDao.deleteByTypeAndContent(SearchContent.SearchType.COMMUNITY, content)
        }
    }

    fun clearAllHistories() {
        viewModelScope.launch {
            searchDao.clearByType(SearchContent.SearchType.COMMUNITY)
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

    fun toggleRecommendVisible() {
        _recommendVisible.value = !(_recommendVisible.value ?: true)
    }
}