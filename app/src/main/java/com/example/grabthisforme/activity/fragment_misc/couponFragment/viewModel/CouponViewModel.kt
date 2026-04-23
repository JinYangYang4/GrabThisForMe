package com.example.grabthisforme.activity.fragment_misc.couponFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CouponViewModel : ViewModel() {
    private val _emptyState = MutableLiveData(false)
    val emptyState: LiveData<Boolean> get() = _emptyState

    fun updateEmptyState(isEmpty: Boolean) {
        _emptyState.value = isEmpty
    }
}
