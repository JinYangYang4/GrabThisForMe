package com.example.grabthisforme.activity.MainActivity.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private  var _drawerOpenState = MutableLiveData<Boolean>(false)
    val drawerOpenState : LiveData<Boolean> get() = _drawerOpenState
    private var _openNewFragment = MutableLiveData<Boolean>(false)
    val openNewFragment : LiveData<Boolean> get()= _openNewFragment
    fun openNewFragment_ture(){
        _openNewFragment.value = true
    }
    fun openNewFragment_false(){
        _openNewFragment.value = false
    }
    fun drawerOpenStateToClose(){
        _drawerOpenState.value = false
    }
    fun drawerOpenStateToOpen(){
        _drawerOpenState.value = true
    }
    private var _page = MutableLiveData<Int>(0)
    val page : LiveData<Int> = _page
    fun toPage(innerPage : Int){
        _page.value = innerPage
    }
}