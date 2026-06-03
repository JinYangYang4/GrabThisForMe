package com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.setfragment.model.data.EditPersonalFormState
import com.example.grabthisforme.activity.fragment_misc.setfragment.model.data.EditPersonalSaveResult
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.domain.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditPersonalInformationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(EditPersonalFormState())
    val formState: StateFlow<EditPersonalFormState> = _formState.asStateFlow()

    private val _saveResult = MutableLiveData<EditPersonalSaveResult>()
    val saveResult: LiveData<EditPersonalSaveResult> = _saveResult

    init {
        observeCurrentUser()
    }

    fun updateAvatar(avatarUri: String) {
        _formState.value = _formState.value.copy(avatarUrl = avatarUri)
    }

    fun saveUserInfo(
        accountName: String,
        phone: String,
        email: String,
        signature: String,
        gender: Int
    ) {
        viewModelScope.launch {
            val currentUser = userRepository.currentUser.value
            val accountNameTrimmed = accountName.trim().ifBlank { currentUser?.accountName.orEmpty() }
            val phoneTrimmed = phone.trim()
            val emailTrimmed = email.trim()
            val signatureTrimmed = signature.trim()
            val avatarUrl = _formState.value.avatarUrl.ifBlank { currentUser?.headPic.orEmpty() }
            val finalDisplayName = accountNameTrimmed

            val updatedUser = User(
                id = currentUser?.id ?: System.currentTimeMillis(),
                name = finalDisplayName,
                headPic = avatarUrl,
                phone = phoneTrimmed.takeIf { it.isNotBlank() },
                email = emailTrimmed.takeIf { it.isNotBlank() },
                gender = gender,
                createTime = currentUser?.createTime ?: System.currentTimeMillis(),
                isVip = currentUser?.isVip ?: false,
                signature = signatureTrimmed.takeIf { it.isNotBlank() },
                isCurrent = true,
                accountName = accountNameTrimmed,
                passwordHash = currentUser?.account?.passwordHash.orEmpty(),
                lastLoginTime = currentUser?.account?.lastLoginTime,
                setting = currentUser?.setting
            )

            runCatching {
                if (currentUser == null) {
                    userRepository.upsertAndSetCurrent(updatedUser)
                } else {
                    userRepository.upsertUser(updatedUser)
                }
            }.onSuccess {
                _saveResult.postValue(
                    EditPersonalSaveResult(
                        success = true,
                        message = "保存成功"
                    )
                )
            }.onFailure {
                _saveResult.postValue(
                    EditPersonalSaveResult(
                        success = false,
                        message = "保存失败: ${it.message ?: "未知错误"}"
                    )
                )
            }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { currentUser ->
                val targetUserId = currentUser?.id ?: _formState.value.userId
                _formState.value = EditPersonalFormState(
                    userId = targetUserId,
                    accountName = currentUser?.accountName.orEmpty(),
                    displayName = currentUser?.name.orEmpty(),
                    phone = currentUser?.phone.orEmpty(),
                    email = currentUser?.email.orEmpty(),
                    signature = currentUser?.signature.orEmpty(),
                    gender = currentUser?.gender ?: UserProfile.GENDER_UNKNOWN,
                    avatarUrl = currentUser?.headPic.orEmpty()
                )
            }
        }
    }
}


