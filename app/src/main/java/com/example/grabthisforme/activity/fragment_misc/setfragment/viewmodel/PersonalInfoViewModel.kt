package com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonalInfoViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _name = MutableLiveData("")
    val name: LiveData<String> get() = _name

    private val _gender = MutableLiveData("未知")
    val gender: LiveData<String> get() = _gender

    private val _region = MutableLiveData("未设置")
    val region: LiveData<String> get() = _region

    private val _mobile = MutableLiveData("")
    val mobile: LiveData<String> get() = _mobile

    private val _account = MutableLiveData("")
    val account: LiveData<String> get() = _account

    private val _signature = MutableLiveData("")
    val signature: LiveData<String> get() = _signature
    private val _headPic = MutableLiveData("")
    val headPic : LiveData<String> get() = _headPic

    init {
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { user ->
                if (user == null) {
                    _name.postValue("")
                    _gender.postValue("未知")
                    _mobile.postValue("")
                    _headPic.postValue("")
                    _account.postValue("")
                    _signature.postValue("")
                    _region.postValue("未设置")
                    return@collectLatest
                }
                _name.postValue(user.name)
                _headPic.postValue(user.headPic)
                _gender.postValue(
                    when (user.gender) {
                        UserProfile.GENDER_MALE -> "男"
                        UserProfile.GENDER_FEMALE -> "女"
                        else -> "未知"
                    }
                )
                _mobile.postValue(user.phone.orEmpty())
                _account.postValue(user.accountName)
                _signature.postValue(user.signature.orEmpty())
                _region.postValue("未设置")
            }
        }
    }
}
