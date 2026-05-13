package com.example.grabthisforme.activity.fragment_misc.create.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.order.data.dao.OrderDao
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderStatusInfo
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val orderDao: OrderDao,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _buyGoodsMode = MutableLiveData(true)
    val buyGoodsMode: LiveData<Boolean> get() = _buyGoodsMode

    private val _startTime = MutableLiveData("")
    val startTime: LiveData<String> get() = _startTime

    private val _endTime = MutableLiveData("")
    val endTime: LiveData<String> get() = _endTime

    private val _expressStartTime = MutableLiveData("")
    val expressStartTime: LiveData<String> get() = _expressStartTime

    private val _expressEndTime = MutableLiveData("")
    val expressEndTime: LiveData<String> get() = _expressEndTime

    private val _goodsPic = MutableLiveData("")
    val goodsPic: LiveData<String> get() = _goodsPic

    private val _createResult = MutableLiveData<CreateOrderResult>()
    val createResult: LiveData<CreateOrderResult> get() = _createResult

    fun setBuyGoodsMode(isBuyGoods: Boolean) {
        _buyGoodsMode.value = isBuyGoods
    }

    fun setStartTime(value: String) {
        _startTime.value = value
    }


    fun setEndTime(value: String) {
        _endTime.value = value
    }

    fun setExpressStartTime(value: String) {
        _expressStartTime.value = value
    }

    fun setExpressEndTime(value: String) {
        _expressEndTime.value = value
    }

    fun setGoodsPic(value: String) {
        _goodsPic.value = value
    }

    fun submitBuyGoodsOrder(
        goodsName: String,
        goodsPriceText: String,
        goodsMessage: String,
        goodsPic: String,
        saleNumber: Long,
        aimPosition: String,
        shelfNumber: String,
        startTimeText: String,
        endTimeText: String
    ) {
        if (goodsName.isBlank() || goodsPriceText.isBlank() || aimPosition.isBlank()) {
            _createResult.value = CreateOrderResult(
                success = false,
                message = "请先填写完整必填项"
            )
            return
        }
        val goodsPrice = goodsPriceText.toDoubleOrNull()
        if (goodsPrice == null || goodsPrice <= 0.0) {
            _createResult.value = CreateOrderResult(
                success = false,
                message = "商品价格格式不正确"
            )
            return
        }

        saveOrderInternal(
            goods = Goods(
                id = System.currentTimeMillis(),
                name = goodsName,
                message = goodsMessage,
                price = goodsPrice,
                sale_number = saleNumber.coerceAtLeast(1L),
                pic = goodsPic
            ),
            shelfNumber = shelfNumber,
            aimPosition = aimPosition,
            atPosition = "",
            startTime = parseDateTimeToMillis(startTimeText),
            endTime = parseDateTimeToMillis(endTimeText)
        )
    }

    fun submitExpressOrder(
        expressNo: String,
        expressCompany: String,
        expressPosition: String,
        pickupCode: String,
        remark: String,
        startTimeText: String,
        endTimeText: String
    ) {
        if (expressNo.isBlank() || expressCompany.isBlank() || expressPosition.isBlank() || pickupCode.isBlank()) {
            _createResult.value = CreateOrderResult(
                success = false,
                message = "请先填写完整必填项"
            )
            return
        }

        val goodsMessage = buildString {
            append("快递单号: ")
            append(expressNo)
            append("；取件码: ")
            append(pickupCode)
            if (remark.isNotBlank()) {
                append("；备注: ")
                append(remark)
            }
        }

        saveOrderInternal(
            goods = Goods(
                id = System.currentTimeMillis(),
                name = "取件代取 - $expressCompany",
                message = goodsMessage,
                price = 0.0,
                pic = ""
            ),
            shelfNumber = pickupCode,
            aimPosition = expressPosition,
            atPosition = expressNo,
            startTime = parseDateTimeToMillis(startTimeText),
            endTime = parseDateTimeToMillis(endTimeText)
        )
    }

    private fun saveOrderInternal(
        goods: Goods,
        shelfNumber: String,
        aimPosition: String,
        atPosition: String,
        startTime: Long,
        endTime: Long
    ) {
        viewModelScope.launch {
            runCatching {
                val buyer = userRepository.currentUser.value ?: buildFallbackUser()
                Log.d("test11", "saveOrderInternal:${userRepository.currentUser.value?.id} ")
                val order = Order(
                    sender = null,
                    orderId = "ORDER_${System.currentTimeMillis()}",
                    buyer = buyer,
                    goods = goods,
                    shelf_number = shelfNumber,
                    aim_position = aimPosition,
                    at_position = atPosition,
                    startTime = startTime,
                    endTime = endTime,
                    orderStatus = OrderStatusInfo.STATUS_PENDING_RECEIPT,
                    isAccepted = false
                )
                orderDao.saveOrder(order)
            }.onSuccess {
                _createResult.postValue(
                    CreateOrderResult(
                        success = true,
                        message = "订单创建成功"
                    )
                )
            }.onFailure {
                _createResult.postValue(
                    CreateOrderResult(
                        success = false,
                        message = "订单创建失败: ${it.message ?: "未知错误"}"
                    )
                )
            }
        }
    }

    private fun parseDateTimeToMillis(timeText: String): Long {
        if (timeText.isBlank()) return 0L
        return runCatching {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
            format.parse(timeText)?.time ?: 0L
        }.getOrDefault(0L)
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

data class CreateOrderResult(
    val success: Boolean,
    val message: String,
    val eventId: Long = System.currentTimeMillis()
)
