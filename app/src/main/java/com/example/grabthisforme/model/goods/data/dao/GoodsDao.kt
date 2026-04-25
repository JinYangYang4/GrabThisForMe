package com.example.grabthisforme.model.goods.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.goods.data.entity.GoodsBaseEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsBundleEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsPriceEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsStateEntity
import com.example.grabthisforme.model.goods.data.entity.GoodsUiEntity
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.goods.mapper.toBaseEntity
import com.example.grabthisforme.model.goods.mapper.toDomain
import com.example.grabthisforme.model.goods.mapper.toDomainSecondhandOrNull
import com.example.grabthisforme.model.goods.mapper.toPriceEntity
import com.example.grabthisforme.model.goods.mapper.toStateEntity
import com.example.grabthisforme.model.goods.mapper.toUiEntity
import com.example.grabthisforme.model.secondhandGoods.data.entity.SecondhandTradeEntity
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.secondhandGoods.mapper.toDomain
import com.example.grabthisforme.model.secondhandGoods.mapper.toTradeEntity

@Dao
interface GoodsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBase(entity: GoodsBaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrice(entity: GoodsPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUi(entity: GoodsUiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertState(entity: GoodsStateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrade(entity: SecondhandTradeEntity)

    @Transaction
    suspend fun saveGoods(goods: Goods) {
        upsertBase(goods.toBaseEntity())
        upsertPrice(goods.toPriceEntity())
        upsertUi(goods.toUiEntity())
        upsertState(goods.toStateEntity())
    }

    @Transaction
    suspend fun saveSecondhandGoods(goods: SecondhandGoods) {
        saveGoods(goods)
        upsertTrade(goods.toTradeEntity())
    }

    @Transaction
    @Query("SELECT * FROM goods_base WHERE goodsId = :goodsId LIMIT 1")
    suspend fun getGoodsBundle(goodsId: Long): GoodsBundleEntity?

    @Transaction
    @Query("SELECT * FROM goods_base ORDER BY goodsId DESC")
    suspend fun getAllGoodsBundles(): List<GoodsBundleEntity>

    suspend fun getAllGoods(): List<Goods> {
        return getAllGoodsBundles().map { it.toDomain() }
    }

    suspend fun getAllSecondhandGoods(): List<SecondhandGoods> {
        return getAllGoodsBundles().mapNotNull { it.toDomainSecondhandOrNull() }
    }

    @Query("DELETE FROM goods_base WHERE goodsId = :goodsId")
    suspend fun deleteGoodsById(goodsId: Long)
}
