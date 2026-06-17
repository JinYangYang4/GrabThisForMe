package com.example.grabthisforme.model.goods.data.repository

import com.example.grabthisforme.model.goods.data.local.dao.GoodsDao
import com.example.grabthisforme.model.goods.data.mock.GoodsSampleData
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDomainSecondhandOrNull
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.user.data.local.dao.UserDao
import com.example.grabthisforme.model.user.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GoodsLocalRepository @Inject constructor(
    private val goodsDao: GoodsDao,
    private val userDao: UserDao
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
        .flatMapLatest { bundles ->
            val sellerIds = bundles.mapNotNull { it.trade?.saleUserId }.distinct()
            if (sellerIds.isEmpty()) {
                flowOf(bundles.mapNotNull { it.toDomainSecondhandOrNull() })
            } else {
                userDao.observeUserBasicBundlesByIds(sellerIds).map { sellerBundles ->
                    val sellersById = sellerBundles
                        .map { it.toDomain() }
                        .associateBy { it.id }
                    bundles.mapNotNull { bundle ->
                        val saleUser = bundle.trade?.saleUserId?.let { sellersById[it] }
                        bundle.toDomainSecondhandOrNull(saleUser)
                    }
                }
            }
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

    fun getGoodsByStoreId(storeId: Long): Flow<List<Goods>> {
        return goodsDao.observeGoodsBundlesByStoreId(storeId)
            .map { bundles -> bundles.map { it.toDomain() } }
    }

    fun getSingleDisplayGoods(): Goods {
        return allGoodsList.value.firstOrNull() ?: GoodsSampleData.getSingleVirtualGoods()
    }
}
