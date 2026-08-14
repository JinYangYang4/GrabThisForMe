package com.example.grabthisforme.activity.fragment_misc.couponFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.coupon.data.network.dto.CouponTemplateDto
import com.example.grabthisforme.model.coupon.data.network.dto.UserCouponDto
import com.example.grabthisforme.model.coupon.data.repository.CouponRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class CouponViewModel @Inject constructor(
    private val repository: CouponRepository
) : ViewModel() {
    private val _emptyState = MutableLiveData(false)
    private val _items = MutableLiveData<List<CouponDisplayItem>>(emptyList())
    private val _loading = MutableLiveData(false)
    private val _message = MutableLiveData<CouponMessage>()

    val emptyState: LiveData<Boolean> get() = _emptyState
    val items: LiveData<List<CouponDisplayItem>> get() = _items
    val loading: LiveData<Boolean> get() = _loading
    val message: LiveData<CouponMessage> get() = _message

    init {
        loadMine()
    }

    fun loadMine() = load { repository.listMine().map(UserCouponDto::toDisplayItem) }

    fun loadMarket() = load { repository.listMarket().map(CouponTemplateDto::toDisplayItem) }

    fun buy(item: CouponDisplayItem) {
        if (!item.isMarket || !item.canPurchase || _loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            runCatching { repository.purchase(item.templateId) }
                .onSuccess {
                    _message.postValue(CouponMessage("购买成功，优惠券已放入券包"))
                    runCatching { repository.listMarket().map(CouponTemplateDto::toDisplayItem) }
                        .onSuccess(::updateItems)
                }
                .onFailure { _message.postValue(CouponMessage(it.message ?: "购买优惠券失败")) }
            _loading.postValue(false)
        }
    }

    private fun load(block: suspend () -> List<CouponDisplayItem>) {
        if (_loading.value == true) return
        _loading.value = true
        viewModelScope.launch {
            runCatching { block() }
                .onSuccess(::updateItems)
                .onFailure {
                    _message.postValue(CouponMessage(it.message ?: "加载优惠券失败"))
                    updateItems(emptyList())
                }
            _loading.postValue(false)
        }
    }

    private fun updateItems(items: List<CouponDisplayItem>) {
        _items.postValue(items)
        _emptyState.postValue(items.isEmpty())
    }
}

data class CouponDisplayItem(
    val key: String,
    val templateId: Long,
    val userCouponId: String? = null,
    val title: String,
    val description: String,
    val discountAmount: Double,
    val minimumAmount: Double,
    val purchasePrice: Double,
    val validDays: Int? = null,
    val validUntil: Long? = null,
    val status: String,
    val stock: Int = 0,
    val canPurchase: Boolean = false,
    val isMarket: Boolean
)

data class CouponMessage(
    val text: String,
    val eventId: Long = System.nanoTime()
)

private fun CouponTemplateDto.toDisplayItem() = CouponDisplayItem(
    key = "template:$templateId",
    templateId = templateId,
    title = title,
    description = description,
    discountAmount = discountAmount,
    minimumAmount = minimumAmount,
    purchasePrice = purchasePrice,
    validDays = validDays,
    status = if (canPurchase) "ON_SALE" else "UNAVAILABLE",
    stock = stock,
    canPurchase = canPurchase,
    isMarket = true
)

private fun UserCouponDto.toDisplayItem() = CouponDisplayItem(
    key = "user:$userCouponId",
    templateId = templateId,
    userCouponId = userCouponId,
    title = title,
    description = description,
    discountAmount = discountAmount,
    minimumAmount = minimumAmount,
    purchasePrice = purchasePricePaid,
    validUntil = validUntil,
    status = status,
    isMarket = false
)
