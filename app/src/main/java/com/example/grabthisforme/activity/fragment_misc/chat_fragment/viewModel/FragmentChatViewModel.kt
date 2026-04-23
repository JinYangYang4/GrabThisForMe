package com.example.grabthisforme.activity.fragment_misc.chat_fragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FragmentChatViewModel : ViewModel() {
    private var _keyboardState : MutableLiveData<Boolean> = MutableLiveData(false)
    val keyboardStatus : LiveData<Boolean> get() = _keyboardState
    private val _peerName = MutableLiveData("昵称")
    val peerName: LiveData<String> get() = _peerName
    private val _canSend = MutableLiveData(false)
    val canSend: LiveData<Boolean> get() = _canSend
    private val _inputText = MutableLiveData("")
    val inputText: LiveData<String> get() = _inputText
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

    fun onInputChanged(input: String) {
        _inputText.value = input
        _canSend.value = input.trim().isNotEmpty()
    }

    fun clearInputState() {
        _inputText.value = ""
        _canSend.value = false
    }
}