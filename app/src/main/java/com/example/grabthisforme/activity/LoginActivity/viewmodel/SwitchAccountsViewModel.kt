package com.example.grabthisforme.activity.LoginActivity.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.LoginActivity.ui_model.SwitchAccountItemUiModel
import com.example.grabthisforme.activity.LoginActivity.ui_model.toSwitchAccountItemUiModel
import com.example.grabthisforme.model.auth.data.repository.AuthRepository
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class SwitchAccountsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _testUserList = MutableLiveData<List<SwitchAccountItemUiModel>>(emptyList())
    val testUserList: LiveData<List<SwitchAccountItemUiModel>> = _testUserList

    private val _switchAccountSuccess = MutableLiveData<User?>()
    val switchAccountSuccess: LiveData<User?> = _switchAccountSuccess

    private val _switchAccountError = MutableLiveData<String?>(null)
    val switchAccountError: LiveData<String?> = _switchAccountError

    private val _deleteAccountMessage = MutableLiveData<String?>(null)
    val deleteAccountMessage: LiveData<String?> = _deleteAccountMessage

    init {
        viewModelScope.launch {
            userRepository.allLoginUsers.collectLatest { users ->
                _testUserList.postValue(users.map { it.toSwitchAccountItemUiModel() })
            }
        }
    }

    fun switchToUser(userId: Long) {
        viewModelScope.launch {
            val targetUser = userRepository.allLoginUsers.value.find { it.id == userId }
            if (targetUser == null) {
                _switchAccountError.postValue("未找到目标账号")
                return@launch
            }

            val password = targetUser.account.passwordHash
            if (password.isBlank()) {
                _switchAccountError.postValue("该账号缺少本地密码，无法自动切换登录")
                return@launch
            }

            authRepository.login(
                identifier = targetUser.accountName,
                password = password
            ).onSuccess { user ->
                _switchAccountSuccess.postValue(user)
            }.onFailure { throwable ->
                _switchAccountError.postValue(throwable.message ?: "切换账号失败")
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            val targetUser = userRepository.allLoginUsers.value.find { it.id == userId }
            if (targetUser == null) {
                _deleteAccountMessage.postValue("账号不存在")
                return@launch
            }
            if (targetUser.isCurrent) {
                _deleteAccountMessage.postValue("当前账号不能在此处删除")
                return@launch
            }

            userRepository.deleteUsersByIds(listOf(userId))
            _deleteAccountMessage.postValue("已删除本地保存账号：${targetUser.name}")
        }
    }

    fun insertUser(user: User) {
        viewModelScope.launch {
            userRepository.upsertUser(user)
        }
    }

    fun onSwitchAccountErrorConsumed() {
        _switchAccountError.value = null
    }

    fun onDeleteAccountMessageConsumed() {
        _deleteAccountMessage.value = null
    }
}
