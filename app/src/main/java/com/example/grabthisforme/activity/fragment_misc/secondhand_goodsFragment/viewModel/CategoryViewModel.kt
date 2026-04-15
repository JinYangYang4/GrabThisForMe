package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.model.ConditionModel

class CategoryViewModel : ViewModel() {
    private val _conditionList = MutableLiveData<MutableList<ConditionModel>>()
    val categoryList: LiveData<MutableList<ConditionModel>> = _conditionList
    private val _selectedCategoryId = MutableLiveData<Long>()
    val selectedCategoryId: LiveData<Long> = _selectedCategoryId

    fun initCategories() {
        val list = mutableListOf(
            ConditionModel(1, "全新"),
            ConditionModel(2, "99新"),
            ConditionModel(3, "95新"),
            ConditionModel(4, "9成新"),
            ConditionModel(5, "8成新"),
            ConditionModel(6, "7成新及以下")
        )
        list[0].isSelected = true
        _conditionList.postValue(list)
        _selectedCategoryId.postValue(list[0].id)
    }

    fun switchCategory(targetId: Long) {
        val oldList = _conditionList.value ?: return
        val newList = oldList.map { oldItem ->
            oldItem.copy(isSelected = oldItem.id == targetId)
        }
        _conditionList.postValue(newList as MutableList<ConditionModel>?)
        _selectedCategoryId.postValue(targetId)
    }
}