package com.example.grabthisforme.activity.fragment_misc.my_store.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.my_store.ui_model.MyStoreListItemUiModel
import com.example.grabthisforme.activity.fragment_misc.my_store.ui_model.toMyStoreListItemUiModel
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _storeList = MutableLiveData<List<MyStoreListItemUiModel>>(emptyList())
    val storeList: LiveData<List<MyStoreListItemUiModel>> get() = _storeList

    private val _storeEmpty = MutableLiveData(true)
    val storeEmpty: LiveData<Boolean> get() = _storeEmpty

    init {
        viewModelScope.launch {
            runCatching { storeRepository.refreshMyStores() }
        }
        viewModelScope.launch {
            storeRepository.myStoreList.collectLatest { stores ->
                val uiList = stores.map { it.toMyStoreListItemUiModel() }
                _storeList.value = uiList
                _storeEmpty.value = uiList.isEmpty()
            }
        }
    }
}
