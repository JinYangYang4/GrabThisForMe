package com.example.grabthisforme.activity.fragment_misc.create.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.create.model.CreateGoodsRegistration
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateGoodsViewModel @Inject constructor(
    private val goodsRepository: GoodsRepository
) : ViewModel() {

    private val _createResult = MutableLiveData<CreateGoodsResult>()
    val createResult: LiveData<CreateGoodsResult> get() = _createResult

    private val _goodsPic = MutableLiveData("")
    val goodsPic: LiveData<String> get() = _goodsPic

    fun setGoodsPic(picUrl: String) {
        _goodsPic.value = picUrl
    }

    fun submitCreateGoods(
        registration: CreateGoodsRegistration,
        storeId: Long
    ) {
        if (storeId <= 0L) {
            _createResult.value = CreateGoodsResult(
                success = false,
                message = "店铺信息无效，无法上架商品"
            )
            return
        }

        val parsed = registration.parseOrError().getOrElse {
            _createResult.value = CreateGoodsResult(
                success = false,
                message = it.message ?: "创建失败"
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                val goods = Goods(
                    id = 0L,
                    storeId = storeId,
                    name = parsed.name,
                    message = parsed.description,
                    price = parsed.price,
                    category = parsed.category,
                    discountPrice = parsed.discountPrice,
                    discountTag = if (parsed.discountPrice > 0) "折扣" else "",
                    tag = parsed.tag,
                    stock = parsed.stock,
                    pic = parsed.imageUrl
                )
                goodsRepository.createGoods(goods)
            }.onSuccess { goods ->
                _createResult.postValue(
                    CreateGoodsResult(
                        success = true,
                        message = "商品创建成功",
                        goods = goods
                    )
                )
            }.onFailure { error ->
                _createResult.postValue(
                    CreateGoodsResult(
                        success = false,
                        message = "创建失败：${error.message ?: "未知错误"}"
                    )
                )
            }
        }
    }
}

data class CreateGoodsResult(
    val success: Boolean,
    val message: String,
    val goods: Goods? = null,
    val eventId: Long = System.currentTimeMillis()
)
