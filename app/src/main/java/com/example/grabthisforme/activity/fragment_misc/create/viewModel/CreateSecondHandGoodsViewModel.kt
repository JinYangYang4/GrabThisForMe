package com.example.grabthisforme.activity.fragment_misc.create.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.viewModel.SecondHandsViewModel
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.goods.domain.GoodsStateInfo
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandTradeInfo
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateSecondHandGoodsViewModel @Inject constructor(
    private val goodsRepository: GoodsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _createResult = MutableLiveData<CreateSecondhandResult>()
    val createResult: LiveData<CreateSecondhandResult> get() = _createResult

    private val _selectedPhotoUri = MutableLiveData<Uri?>()
    val selectedPhotoUri: LiveData<Uri?> get() = _selectedPhotoUri

    private val _qualityList = MutableLiveData<List<String>>(
        SecondHandsViewModel.createCreateConditionList().map { it.conditionText }
    )
    val qualityList: LiveData<List<String>> get() = _qualityList

    fun selectPhoto(uri: Uri) {
        _selectedPhotoUri.value = uri
    }

    fun submitSecondhandGoods(
        name: String,
        message: String,
        secondhandPriceText: String,
        originalPriceText: String,
        quality: String,
        usedTime: String,
        remark: String,
        saleNumberText: String,
        pic: String,
        categoryText: String
    ) {
        if (name.isBlank() || message.isBlank() || secondhandPriceText.isBlank() || quality.isBlank()) {
            _createResult.value = CreateSecondhandResult(false, "请先填写完整必填项")
            return
        }
        if (pic.isBlank()) {
            _createResult.value = CreateSecondhandResult(false, "璇烽€夋嫨鍟嗗搧鍥剧墖")
            return
        }
        if (categoryText.isBlank() || categoryText == "璇烽€夋嫨鍟嗗搧绫诲埆") {
            _createResult.value = CreateSecondhandResult(false, "璇烽€夋嫨鍟嗗搧绫诲埆")
            return
        }

        val secondhandPrice = secondhandPriceText.toDoubleOrNull()
        if (secondhandPrice == null || secondhandPrice <= 0.0) {
            _createResult.value = CreateSecondhandResult(false, "二手价格格式不正确")
            return
        }

        val originalPrice = originalPriceText.toDoubleOrNull() ?: secondhandPrice
        val saleNumber = saleNumberText.toLongOrNull()?.coerceAtLeast(1L) ?: 1L
        val category = mapCategory(categoryText)
        val enhancedMessage = if (remark.isNotBlank()) "$message\n备注：$remark" else message

        viewModelScope.launch {
            runCatching {
                val saleUser = userRepository.currentUser.value ?: buildFallbackUser()
                val goods = SecondhandGoods(
                    saleUser = saleUser,
                    id = System.currentTimeMillis(),
                    name = name,
                    message = enhancedMessage,
                    category = category,
                    secondhandPrice = secondhandPrice,
                    sale_number = saleNumber,
                    pic = pic,
                    originalPrice = originalPrice,
                    quality = quality,
                    usedTime = usedTime.takeIf { it.isNotBlank() },
                    tradeStatus = SecondhandTradeInfo.STATUS_ON_SALE,
                    negotiable = true,
                    purchaseStatus = GoodsStateInfo.PURCHASE_STATUS_NO_PURCHASE,
                    soldCount = 0L
                )
                goodsRepository.saveSecondhandGoods(goods)
            }.onSuccess {
                _createResult.postValue(CreateSecondhandResult(true, "浜屾墜鍟嗗搧鍙戝竷鎴愬姛"))
            }.onFailure {
                _createResult.postValue(
                    CreateSecondhandResult(false, "鍙戝竷澶辫触: ${it.message ?: "鏈煡閿欒"}")
                )
            }
        }
    }

    private fun buildFallbackUser(): User {
        val now = System.currentTimeMillis()
        return User(
            id = now,
            name = "娓稿",
            headPic = "",
            passwordHash = "",
            isCurrent = true
        )
    }

    private fun mapCategory(categoryText: String): Goods.GoodsCategory {
        return when (categoryText.trim()) {
            "鏁扮爜浜у搧" -> Goods.GoodsCategory.DIGITAL
            "鏈嶉グ闉嬪附" -> Goods.GoodsCategory.CLOTHING
            "瀹跺眳鐢ㄥ搧" -> Goods.GoodsCategory.HOME
            "鍥句功鏂囧叿" -> Goods.GoodsCategory.BOOK
            "缇庡鎶よ偆" -> Goods.GoodsCategory.BEAUTY
            "杩愬姩鍣ㄦ潗" -> Goods.GoodsCategory.SPORT
            "椋熷搧" -> Goods.GoodsCategory.FOOD
            else -> Goods.GoodsCategory.OTHER
        }
    }
}

data class CreateSecondhandResult(
    val success: Boolean,
    val message: String,
    val eventId: Long = System.currentTimeMillis()
)
