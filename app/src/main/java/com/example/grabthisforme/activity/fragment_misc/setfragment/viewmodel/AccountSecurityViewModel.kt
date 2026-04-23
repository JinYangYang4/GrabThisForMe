package com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountSecurityViewModel : ViewModel() {
    private val _account = MutableLiveData("user12345678")
    val account: LiveData<String> get() = _account
}
