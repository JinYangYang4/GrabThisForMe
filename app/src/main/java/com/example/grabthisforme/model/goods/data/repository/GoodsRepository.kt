package com.example.grabthisforme.model.goods.data.repository

import com.example.grabthisforme.model.goods.data.dao.GoodsDao
import com.example.grabthisforme.model.goods.data.mock.GoodsSampleData
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDomainSecondhandOrNull
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoodsRepository @Inject constructor(
    private val goodsDao: GoodsDao
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sourceGoodsBundles = goodsDao.observeAllGoodsBundles()
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val allGoodsList: StateFlow<List<Goods>> = sourceGoodsBundles
        .map { bundles ->
            val list = bundles.map { it.toDomain() }
            if (list.isEmpty()) GoodsSampleData.get20RepeatGoods() else list
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = GoodsSampleData.get20RepeatGoods()
        )

    val secondhandGoodsList: StateFlow<List<SecondhandGoods>> = sourceGoodsBundles
        .map { bundles ->
            bundles.mapNotNull { it.toDomainSecondhandOrNull() }
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        repositoryScope.launch {
            val cachedGoods = goodsDao.observeAllGoodsBundles().first()
            if (cachedGoods.isEmpty()) {
                goodsDao.saveGoodsBatch(GoodsSampleData.get20RepeatGoods())
            }
        }
    }

    suspend fun saveGoods(goods: Goods) {
        goodsDao.saveGoods(goods)
    }

    suspend fun saveGoodsBatch(goodsList: List<Goods>) {
        goodsDao.saveGoodsBatch(goodsList)
    }

    suspend fun saveSecondhandGoods(goods: SecondhandGoods) {
        goodsDao.saveSecondhandGoods(goods)
    }

    suspend fun deleteGoodsById(goodsId: Long) {
        goodsDao.deleteGoodsById(goodsId)
    }

    suspend fun getGoodsById(goodsId: Long): Goods? {
        return goodsDao.getGoodsBundle(goodsId)?.toDomain()
    }

    fun getSingleDisplayGoods(): Goods {
        return allGoodsList.value.firstOrNull() ?: GoodsSampleData.getSingleVirtualGoods()
    }
}
