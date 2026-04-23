package com.example.grabthisforme.activity.mainactivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrderMessageViewModel : ViewModel() {
    private val _buyerName = MutableLiveData("")
    val buyerName: LiveData<String> get() = _buyerName

    fun updateBuyerName(name: String) {
        _buyerName.value = name
    }
}
