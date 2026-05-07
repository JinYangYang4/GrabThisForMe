package com.example.grabthisforme.activity.LoginActivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _testUserList = MutableLiveData<MutableList<User>>()
    val testUserList: LiveData<MutableList<User>> = _testUserList

    init {
        viewModelScope.launch {
            userRepository.allLoginUsers.collectLatest { users ->
                _testUserList.postValue(users.toMutableList())
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
            userDao.loginAndSetCurrent(user)
        }
    }
}
