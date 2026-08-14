package com.example.grabthisforme.model.goods.data.repository

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import com.example.grabthisforme.model.goods.data.network.api.CreateGoodsRequest
import com.example.grabthisforme.model.goods.mapper.toDomain

@Singleton
class GoodsRepository @Inject constructor(
    private val localRepository: GoodsLocalRepository,
    private val remoteRepository: GoodsRemoteRepository
) {
    val allGoodsList = localRepository.allGoodsList
    val secondhandGoodsList = localRepository.secondhandGoodsList

    suspend fun saveGoods(goods: Goods) = localRepository.saveGoods(goods)
    suspend fun createGoods(goods: Goods): Goods {
        val created = remoteRepository.createGoods(
            CreateGoodsRequest(
                storeId = goods.storeId,
                name = goods.name,
                message = goods.message,
                categoryKey = goods.category?.name,
                price = goods.price,
                discountPrice = goods.discountPrice,
                discountTag = goods.discountTag,
                pic = goods.pic,
                tag = goods.tag,
                unit = goods.unit,
                stock = goods.stock,
                isHot = goods.isHot
            )
        ).toDomain()
        localRepository.saveGoods(created)
        return created
    }
    suspend fun saveGoodsBatch(goodsList: List<Goods>) = localRepository.saveGoodsBatch(goodsList)
    suspend fun saveSecondhandGoods(goods: SecondhandGoods) = localRepository.saveSecondhandGoods(goods)
    suspend fun deleteGoodsById(goodsId: Long) = localRepository.deleteGoodsById(goodsId)
    suspend fun getGoodsById(goodsId: Long): Goods? = localRepository.getGoodsById(goodsId)
    fun getGoodsByStoreId(storeId: Long): Flow<List<Goods>> = localRepository.getGoodsByStoreId(storeId)
    fun getSingleDisplayGoods(): Goods = localRepository.getSingleDisplayGoods()
}
