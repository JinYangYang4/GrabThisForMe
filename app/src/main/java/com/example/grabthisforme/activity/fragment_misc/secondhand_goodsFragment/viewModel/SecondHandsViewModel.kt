package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.model.ConditionModel
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecondHandsViewModel @Inject constructor(
    goodsRepository: GoodsRepository
) : ViewModel() {

    private val _conditionList = MutableLiveData<MutableList<ConditionModel>>()
    val categoryList: LiveData<MutableList<ConditionModel>> = _conditionList

    private val _selectedCategoryId = MutableLiveData<Long>()
    val selectedCategoryId: LiveData<Long> = _selectedCategoryId

    private val selectedGoodsCategory = MutableStateFlow<Goods.GoodsCategory?>(null)
    private val selectedQuality = MutableStateFlow(QUALITY_ALL)

    private val _filteredGoodsList = MutableLiveData<List<SecondhandGoods>>(emptyList())
    val filteredGoodsList: LiveData<List<SecondhandGoods>> = _filteredGoodsList

    init {
        viewModelScope.launch {
            combine(
                goodsRepository.secondhandGoodsList,
                selectedGoodsCategory,
                selectedQuality
            ) { goodsList, goodsCategory, quality ->
                if (goodsCategory == null) {
                    return@combine emptyList()
                }
                goodsList.filter { goods ->
                    val categoryMatched = goods.category == goodsCategory
                    val qualityMatched = quality == QUALITY_ALL || goods.quality == quality
                    categoryMatched && qualityMatched
                }
            }.collect { filteredGoods ->
                _filteredGoodsList.value = filteredGoods
            }
        }
    }

    fun setGoodsCategory(category: Goods.GoodsCategory) {
        selectedGoodsCategory.value = category
    }

    fun initCategories() {
        val list = createDisplayConditionList()
        list.firstOrNull()?.isSelected = true
        _conditionList.value = list
        _selectedCategoryId.value = list.firstOrNull()?.id
        selectedQuality.value = list.firstOrNull()?.conditionText.orEmpty()
    }

    fun switchCategory(targetId: Long) {
        val oldList = _conditionList.value ?: return
        val newList = oldList.map { oldItem ->
            oldItem.copy(isSelected = oldItem.id == targetId)
        }.toMutableList()
        val selectedCondition = newList.firstOrNull { it.id == targetId } ?: return

        _conditionList.value = newList
        _selectedCategoryId.value = targetId
        selectedQuality.value = selectedCondition.conditionText
    }

    companion object {
        private const val QUALITY_ALL = "全部"

        fun createCreateConditionList(): MutableList<ConditionModel> {
            return mutableListOf(
                ConditionModel(1, "全新"),
                ConditionModel(2, "99新"),
                ConditionModel(3, "95新"),
                ConditionModel(4, "9成新"),
                ConditionModel(5, "8成新"),
                ConditionModel(6, "7成新及以下")
            )
        }

        fun createDisplayConditionList(): MutableList<ConditionModel> {
            return (mutableListOf(ConditionModel(0, QUALITY_ALL)) + createCreateConditionList()).toMutableList()
        }
    }
}
