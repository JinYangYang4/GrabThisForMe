package com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchDao


class SearchFriendOrGroupOrConversationFactory(
    private val searchHistoryDao: SearchDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchFriendOrGroupOrConversationViewModel::class.java)) {
            return SearchFriendOrGroupOrConversationViewModel(searchHistoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}