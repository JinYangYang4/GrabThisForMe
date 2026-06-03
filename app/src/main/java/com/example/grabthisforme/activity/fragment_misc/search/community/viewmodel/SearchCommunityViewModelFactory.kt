package com.example.grabthisforme.activity.fragment_misc.search.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchDao


class SearchCommunityViewModelFactory(
    private val searchHistoryDao: SearchDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchCommunityViewModel::class.java)) {
            return SearchCommunityViewModel(searchHistoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
