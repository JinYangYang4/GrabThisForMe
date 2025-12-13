package com.example.grabthisforme.activity.homeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentHomeViewModel : ViewModel() {
    private var isAnimating = false
    private var alreadyShow = false
    private var rvTaskHeight = 0
    private var _rvTaskIsOpen = MutableLiveData<Boolean>(false)
    val rvTaskIsOpen : LiveData<Boolean> get() = _rvTaskIsOpen

    fun setRvTaskHeight(height : Int){
        rvTaskHeight = height
    }
    fun getRvTaskHeight(): Int {
        return rvTaskHeight
    }
    fun MakeIsAnimatingTrue(){
        isAnimating = true
    }
    fun MakeIsAnimatingFalse(){
        isAnimating = false
    }
    fun GetIsAnimating(): Boolean {
        return isAnimating
    }


    fun MakeAlreadyShowTure(){
        alreadyShow = true
    }
    fun MakeAlreadyShowFalse(){
        alreadyShow = false
    }
    fun GetAlreadyShow(): Boolean {
        return alreadyShow
    }


    fun MakeRvTaskIsOpenTure(){
        _rvTaskIsOpen.value = true
    }
    fun MakeRvTaskIsOpenFalse(){
        _rvTaskIsOpen.value = false
    }
    fun GetRvTaskIsOpen(): Boolean {
        return _rvTaskIsOpen.value == true
    }
}