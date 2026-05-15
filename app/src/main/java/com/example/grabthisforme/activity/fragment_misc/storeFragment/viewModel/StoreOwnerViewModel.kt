package com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.data.repository.StoreRepository
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.domain.StoreGoodsGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StoreOwnerViewModel @Inject constructor(
    private val storeRepository: StoreRepository
) : ViewModel() {
    private val _showStorePage = MutableLiveData(false)
    private val selectedStoreId = MutableStateFlow<Long?>(null)

    val showStorePage: LiveData<Boolean> get() = _showStorePage

    val currentStore: StateFlow<Store?> = selectedStoreId
        .flatMapLatest { storeId ->
            if (storeId == null || storeId <= 0L) {
                flowOf(null)
            } else {
                storeRepository.getStoreFlow(storeId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val goodsList: StateFlow<List<Goods>> = currentStore
        .map { it?.goodsAll.orEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val categoryList: StateFlow<List<String>> = currentStore
        .map { store ->
            val categories = store?.category.orEmpty()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
            if (categories.isEmpty()) listOf("全部") else categories
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = listOf("全部")
        )

    fun loadStore(storeId: Long) {
        selectedStoreId.value = storeId.takeIf { it > 0L }
    }

    fun setShowStorePage(showStore: Boolean) {
        _showStorePage.value = showStore
    }

    fun addGoodsToCategory(
        store: Store,
        category: String,
        goods: Goods
    ): Store {
        val normalizedCategory = category.trim()
        if (normalizedCategory.isBlank()) {
            return store
        }

        val groups = store.goodsGroups.toMutableList()
        val index = groups.indexOfFirst { it.category == normalizedCategory }

        if (index >= 0) {
            val targetGroup = groups[index]
            val goodsList = targetGroup.goods.toMutableList()
            val goodsIndex = goodsList.indexOfFirst { it.id == goods.id }
            if (goodsIndex >= 0) {
                goodsList[goodsIndex] = goods
            } else {
                goodsList.add(goods)
            }
            groups[index] = targetGroup.copy(goods = goodsList)
        } else {
            groups.add(
                StoreGoodsGroup(
                    category = normalizedCategory,
                    goods = listOf(goods)
                )
            )
        }

        return store.copy(goodsGroups = groups)
    }
}
