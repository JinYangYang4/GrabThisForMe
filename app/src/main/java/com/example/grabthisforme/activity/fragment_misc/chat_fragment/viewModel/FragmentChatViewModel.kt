package com.example.grabthisforme.activity.fragment_misc.chat_fragment.viewModel

import android.R
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentChatViewModel : ViewModel() {
    private var _keyboardState : MutableLiveData<Boolean> = MutableLiveData(false)
    val keyboardStatus : LiveData<Boolean> get() = _keyboardState
    fun turnKeyboardStateToTure(){
        val currentValue = _keyboardState.value
        if (currentValue != true) {
            _keyboardState.value = true
        }
    }

    fun turnKeyboardStateToFalse(){
        val currentValue = _keyboardState.value
        if (currentValue != false) {
            _keyboardState.value = false
        }
    }
}