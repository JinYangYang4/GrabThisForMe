package com.example.grabthisforme.activity.fragment_misc.register_store.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class RegisterStoreViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _ownerIdText = MutableLiveData("")
    val ownerIdText: LiveData<String> get() = _ownerIdText

    private val _startTime = MutableLiveData("")
    val startTime: LiveData<String> get() = _startTime

    private val _endTime = MutableLiveData("")
    val endTime: LiveData<String> get() = _endTime
    private var _currentUser = MutableLiveData<User?>()
    val currentUser : LiveData<User?> get() = _currentUser

    private val _createResult = MutableLiveData<RegisterStoreResult>()
    val createResult: LiveData<RegisterStoreResult> get() = _createResult

    init {
        viewModelScope.launch {
            userRepository.currentUser.collectLatest { currentUser ->
                _ownerIdText.value = currentUser?.id?.toString().orEmpty()
                _currentUser.postValue(currentUser)
            }
        }
    }

    fun setStartTime(value: String) {
        _startTime.value = value
    }

    fun setEndTime(value: String) {
        _endTime.value = value
    }

    fun submitRegisterStore(
        storeName: String,
        storeType: String,
        storeAddress: String,
        phone: String,
        startTimeText: String,
        endTimeText: String,
        minOrderAmountText: String,
        deliveryFeeText: String,
        pic: String,
        tagsText: String,
        isOpen: Boolean
    ) {
        if (storeName.isBlank() || storeType.isBlank() || storeAddress.isBlank() || phone.isBlank()) {
            _createResult.value = RegisterStoreResult(
                success = false,
                message = "请先填写完整必填项"
            )
            return
        }

        if (startTimeText.isNotBlank() xor endTimeText.isNotBlank()) {
            _createResult.value = RegisterStoreResult(
                success = false,
                message = "营业时间请同时选择开始和结束时间"
            )
            return
        }

        val minOrderAmount = parseBigDecimal(minOrderAmountText)
        val deliveryFee = parseBigDecimal(deliveryFeeText)
        val businessHours = if (startTimeText.isBlank() && endTimeText.isBlank()) {
            null
        } else {
            "$startTimeText - $endTimeText"
        }
        val tags = tagsText
            .split(Regex("[,;\\uFF0C\\uFF1B\\u3001]"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        viewModelScope.launch {
            runCatching {
                val owner = userRepository.currentUser.value ?: buildFallbackUser()
                Store(
                    id = System.currentTimeMillis(),
                    ownerId = owner.id,
                    name = storeName,
                    type = storeType,
                    address = storeAddress,
                    phone = phone,
                    businessHours = businessHours,
                    minOrderAmount = minOrderAmount,
                    deliveryFee = deliveryFee,
                    isOpen = isOpen,
                    pic = pic.takeIf { it.isNotBlank() },
                    tags = tags
                )
            }.onSuccess { store ->
                _createResult.postValue(
                    RegisterStoreResult(
                        success = true,
                        message = "店铺注册成功",
                        store = store
                    )
                )
            }.onFailure {
                _createResult.postValue(
                    RegisterStoreResult(
                        success = false,
                        message = "店铺注册失败: ${it.message ?: "未知错误"}"
                    )
                )
            }
        }
    }

    private fun parseBigDecimal(value: String): BigDecimal {
        val cleanedValue = value.trim().replace(",", "")
        if (cleanedValue.isBlank()) return BigDecimal.ZERO
        return runCatching { BigDecimal(cleanedValue) }.getOrDefault(BigDecimal.ZERO)
    }

    private fun buildFallbackUser(): User {
        val now = System.currentTimeMillis()
        return User(
            id = now,
            name = "GuestUser",
            headPic = "",
            isCurrent = true
        )
    }
}

data class RegisterStoreResult(
    val success: Boolean,
    val message: String,
    val store: Store? = null,
    val eventId: Long = System.currentTimeMillis()
)
