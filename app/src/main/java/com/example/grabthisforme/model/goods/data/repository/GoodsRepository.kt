package com.example.grabthisforme.model.goods.data.repository

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoodsRepository @Inject constructor(
    private val localRepository: GoodsLocalRepository,
    private val remoteRepository: GoodsRemoteRepository
) {
    val allGoodsList = localRepository.allGoodsList
    val secondhandGoodsList = localRepository.secondhandGoodsList

    suspend fun saveGoods(goods: Goods) = localRepository.saveGoods(goods)
    suspend fun saveGoodsBatch(goodsList: List<Goods>) = localRepository.saveGoodsBatch(goodsList)
    suspend fun saveSecondhandGoods(goods: SecondhandGoods) = localRepository.saveSecondhandGoods(goods)
    suspend fun deleteGoodsById(goodsId: Long) = localRepository.deleteGoodsById(goodsId)
    suspend fun getGoodsById(goodsId: Long): Goods? = localRepository.getGoodsById(goodsId)
    fun getGoodsByStoreId(storeId: Long): Flow<List<Goods>> = localRepository.getGoodsByStoreId(storeId)
    fun getSingleDisplayGoods(): Goods = localRepository.getSingleDisplayGoods()
}
