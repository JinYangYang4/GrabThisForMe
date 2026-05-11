package com.example.grabthisforme.activity.fragment_misc.my_store.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _storeList = MutableLiveData<List<Store>>(emptyList())
    val storeList: LiveData<List<Store>> get() = _storeList

    private val _storeEmpty = MutableLiveData(true)
    val storeEmpty: LiveData<Boolean> get() = _storeEmpty

    init {
        viewModelScope.launch {
            storeRepository.myStoreList.collectLatest { stores ->
                _storeList.value = stores
                _storeEmpty.value = stores.isEmpty()
            }
        }
    }
}
