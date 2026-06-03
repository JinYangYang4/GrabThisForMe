package com.example.grabthisforme.activity.LoginActivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.LoginActivity.ui_model.SwitchAccountItemUiModel
import com.example.grabthisforme.activity.LoginActivity.ui_model.toSwitchAccountItemUiModel
import com.example.grabthisforme.model.user.data.dao.UserDao
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwitchAccountsViewModel @Inject constructor(
    private val userDao: UserDao,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _testUserList = MutableLiveData<List<SwitchAccountItemUiModel>>(emptyList())
    val testUserList: LiveData<List<SwitchAccountItemUiModel>> = _testUserList

    private val _switchAccountSuccess = MutableLiveData<User?>()
    val switchAccountSuccess: LiveData<User?> = _switchAccountSuccess

    init {
        viewModelScope.launch {
            userRepository.allLoginUsers.collectLatest { users ->
                _testUserList.postValue(users.map { it.toSwitchAccountItemUiModel() })
            }
        }
    }

    fun switchToUser(userId: Long) {
        viewModelScope.launch {
            val allUsers = userRepository.allLoginUsers.value
            val targetUser = allUsers.find { it.id == userId }

            if (targetUser != null) {
                userRepository.upsertAndSetCurrent(targetUser)
                _switchAccountSuccess.postValue(targetUser)
            } else {
                _switchAccountSuccess.postValue(null)
            }
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUserById(user.id)
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch {
            userRepository.upsertUser(user)
        }
    }
}
