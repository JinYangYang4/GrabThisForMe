package com.example.grabthisforme.activity.mainactivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private var _drawerOpenState = MutableLiveData(false)
    val drawerOpenState: LiveData<Boolean> get() = _drawerOpenState

    private var _openNewFragment = MutableLiveData(false)
    val openNewFragment: LiveData<Boolean> get() = _openNewFragment

    private var _selectedTab = MutableLiveData(0)
    val selectedTab: LiveData<Int> = _selectedTab

    private var _page = MutableLiveData(0)
    val page: LiveData<Int> = _page

    private val _drawerUserName = MutableLiveData("作者")
    val drawerUserName: LiveData<String> = _drawerUserName

    private val _drawerAccountText = MutableLiveData("账号：1233231")
    val drawerAccountText: LiveData<String> = _drawerAccountText

    fun openNewFragment_ture() {
        _openNewFragment.value = true
    }

    fun openNewFragment_false() {
        _openNewFragment.value = false
    }

    fun drawerOpenStateToClose() {
        _drawerOpenState.value = false
    }

    fun drawerOpenStateToOpen() {
        _drawerOpenState.value = true
    }

    fun toPage(innerPage: Int) {
        _page.value = innerPage
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun updateDrawerProfile(name: String, account: String) {
        _drawerUserName.value = name
        _drawerAccountText.value = account
    }
}