package com.example.grabthisforme.activity.LoginActivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.user.User
import com.example.grabthisforme.model.user.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwitchAccountsViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _testUserList = MutableLiveData<MutableList<User>>()
    val testUserList: LiveData<MutableList<User>> = _testUserList
    private var allUser : MutableList<User> = mutableListOf()

    init {
        loadTestUserItems()
    }
    private fun loadTestUserItems() {
        viewModelScope.launch {
            allUser = userDao.getAllLoginUsers().toMutableList()
            _testUserList.postValue(allUser)
        }
    }



    fun deleteUser(user: User) {
        viewModelScope.launch {
            userDao.deleteUserById(user.id)
        }
        allUser = allUser.filter { it.id != user.id }.toMutableList()
        _testUserList.value = allUser
    }

    fun insertUser(user: User) {
        val updatedList = allUser.map {
            it.withCurrent(false)
        }.toMutableList()

        val newUser = user.withCurrent(true)
        updatedList.removeAll { it.id == newUser.id }
        updatedList.add(0, newUser)
        allUser = updatedList
        _testUserList.postValue(allUser)

        viewModelScope.launch {
            userDao.loginAndSetCurrent(user)
        }
    }
}
