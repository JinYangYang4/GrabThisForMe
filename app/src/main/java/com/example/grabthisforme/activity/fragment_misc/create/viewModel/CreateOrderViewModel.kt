package com.example.grabthisforme.activity.fragment_misc.create.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateOrderViewModel : ViewModel() {
    private val _buyGoodsMode = MutableLiveData(true)
    val buyGoodsMode: LiveData<Boolean> get() = _buyGoodsMode

    private val _startTime = MutableLiveData("")
    val startTime: LiveData<String> get() = _startTime

    private val _endTime = MutableLiveData("")
    val endTime: LiveData<String> get() = _endTime

    private val _expressStartTime = MutableLiveData("")
    val expressStartTime: LiveData<String> get() = _expressStartTime

    private val _expressEndTime = MutableLiveData("")
    val expressEndTime: LiveData<String> get() = _expressEndTime

    fun setBuyGoodsMode(isBuyGoods: Boolean) {
        _buyGoodsMode.value = isBuyGoods
    }

    fun setStartTime(value: String) {
        _startTime.value = value
    }

    fun setEndTime(value: String) {
        _endTime.value = value
    }

    fun setExpressStartTime(value: String) {
        _expressStartTime.value = value
    }

    fun setExpressEndTime(value: String) {
        _expressEndTime.value = value
    }
}
