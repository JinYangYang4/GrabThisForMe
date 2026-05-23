package com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSecurityViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _account = MutableLiveData("")
    val account: LiveData<String> get() = _account

    private val _canSubmit = MutableLiveData(false)
    val canSubmit: LiveData<Boolean> get() = _canSubmit

    private val _submitResult = MutableLiveData<AccountSecurityResult>()
    val submitResult: LiveData<AccountSecurityResult> get() = _submitResult

    private var currentUser: User? = null
    private var originalPasswordInput: String = ""
    private var newPasswordInput: String = ""

    init {
        observeCurrentUser()
    }

    fun updateOriginalPassword(password: String) {
        originalPasswordInput = password
        refreshSubmitState()
    }

    fun updateNewPassword(password: String) {
        newPasswordInput = password
        refreshSubmitState()
    }

    fun submitPasswordChange() {
        if (_canSubmit.value != true) return
        val user = currentUser
        if (user == null) {
            _submitResult.value = AccountSecurityResult(
                success = false,
                message = "账户错误"
            )
            return
        }

        val updatedUser = user.copy(
            account = user.account.copy(passwordHash = newPasswordInput.trim())
        )

        viewModelScope.launch {
            runCatching {
                userRepository.upsertUser(updatedUser)
            }.onSuccess {
                originalPasswordInput = ""
                newPasswordInput = ""
                refreshSubmitState()
                _submitResult.postValue(
                    AccountSecurityResult(
                        success = true,
                        message = "密码更新成功"
                    )
                )
            }.onFailure {
                _submitResult.postValue(
                    AccountSecurityResult(
                        success = false,
                        message = "更新出错: ${it.message ?: "未知错误"}"
                    )
                )
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { user ->
                currentUser = user
                _account.postValue(user?.accountName.orEmpty())
                refreshSubmitState()
            }
        }
    }

    private fun refreshSubmitState() {
        val user = currentUser
        val originalPassword = originalPasswordInput.trim()
        val newPassword = newPasswordInput.trim()
        val canSubmitValue = user != null &&
            originalPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            originalPassword == user.account.passwordHash &&
            newPassword != user.account.passwordHash &&
            isPasswordValid(newPassword)
        _canSubmit.value = canSubmitValue
        Log.d("pwdCheck", "user是否为空: ${user == null}")
        Log.d("pwdCheck", "原密码非空: ${originalPassword.isNotBlank()}")
        Log.d("pwdCheck", "新密码非空: ${newPassword.isNotBlank()}")
        Log.d("pwdCheck", "原密码匹配哈希: ${user?.account?.passwordHash}")
        Log.d("pwdCheck", "新旧密码不一致: ${newPassword != user?.account?.passwordHash}")
        Log.d("pwdCheck", "新密码格式合法: ${isPasswordValid(newPassword)}")
        Log.d("pwdCheck", "最终能否提交: $canSubmitValue")
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length !in 8..16) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}

data class AccountSecurityResult(
    val success: Boolean,
    val message: String,
    val eventId: Long = System.currentTimeMillis()
)
