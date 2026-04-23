package com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PersonalInfoViewModel : ViewModel() {
    private val _name = MutableLiveData("张三")
    val name: LiveData<String> get() = _name
    private val _gender = MutableLiveData("男")
    val gender: LiveData<String> get() = _gender
    private val _region = MutableLiveData("北京市 朝阳区")
    val region: LiveData<String> get() = _region
    private val _mobile = MutableLiveData("138****8888")
    val mobile: LiveData<String> get() = _mobile
    private val _account = MutableLiveData("user12345678")
    val account: LiveData<String> get() = _account
    private val _signature = MutableLiveData("人生如逆旅，我亦是行人")
    val signature: LiveData<String> get() = _signature
}
