package com.example.grabthisforme.activity.fragment_misc.search.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchDao

class SearchViewModelFactory(
    private val searchHistoryDao: SearchDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(searchHistoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
