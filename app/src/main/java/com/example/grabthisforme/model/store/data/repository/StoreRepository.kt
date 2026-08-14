package com.example.grabthisforme.model.store.data.repository

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.store.domain.Store
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import com.example.grabthisforme.model.store.data.network.api.CreateStoreRequest
import com.example.grabthisforme.model.store.data.network.api.UpdateStoreCategoriesRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val localRepository: StoreLocalRepository,
    private val remoteRepository: StoreRemoteRepository
) {
    val currentUser = localRepository.currentUser
    val currentUserId = localRepository.currentUserId
    val allStoreList = localRepository.allStoreList
    val myStoreList = localRepository.myStoreList

    suspend fun saveStore(store: Store) = localRepository.saveStore(store)
    suspend fun updateStore(store: Store) = localRepository.updateStore(store)
    suspend fun saveStores(stores: List<Store>) = localRepository.saveStores(stores)
    suspend fun deleteStore(storeId: Long) = localRepository.deleteStore(storeId)
    fun getStoreFlow(storeId: Long): Flow<Store?> = localRepository.getStoreFlow(storeId)
    suspend fun getStore(storeId: Long): Store? = localRepository.getStore(storeId)
    fun observeStoreCategories(storeId: Long): Flow<List<String>> = localRepository.observeStoreCategories(storeId)
    fun observeGoodsByStoreAndCategory(storeId: Long, category: String): Flow<List<Goods>> =
        localRepository.observeGoodsByStoreAndCategory(storeId, category)
    fun observeGoodsOutsideSelectedCategory(storeId: Long, category: String): Flow<List<Goods>> =
        localRepository.observeGoodsOutsideSelectedCategory(storeId, category)

    suspend fun updateStoreCategoriesOnly(
        storeId: Long,
        categories: List<String>,
        renamedCategories: Map<String, String> = emptyMap()
    ) {
        val dto = remoteRepository.updateCategories(
            storeId,
            UpdateStoreCategoriesRequest(categories, renamedCategories)
        )
        localRepository.cacheRemoteStore(dto)
    }

    suspend fun assignGoodsToCategory(storeId: Long, goodsId: Long, category: String) {
        localRepository.cacheRemoteStore(remoteRepository.assignGoodsCategory(storeId, goodsId, category))
    }

    suspend fun moveGoodsToUnclassified(storeId: Long, goodsId: Long) {
        localRepository.cacheRemoteStore(remoteRepository.assignGoodsCategory(storeId, goodsId, null))
    }

    suspend fun refreshStore(storeId: Long): Store =
        localRepository.cacheRemoteStore(remoteRepository.getStore(storeId))

    suspend fun refreshMyStores(): List<Store> = remoteRepository.listMyStores().map {
        localRepository.cacheRemoteStore(it)
    }

    suspend fun registerStore(
        name: String,
        type: String,
        address: String,
        categories: List<String> = emptyList()
    ): Store = localRepository.cacheRemoteStore(
        remoteRepository.createStore(
            CreateStoreRequest(name = name, type = type, address = address, categories = categories)
        )
    )

    suspend fun registerStore(
        name: String,
        type: String,
        address: String,
        phone: String?,
        businessHours: String?,
        minOrderAmount: BigDecimal,
        deliveryFee: BigDecimal,
        isOpen: Boolean,
        pic: String?,
        tags: List<String>,
        categories: List<String> = emptyList()
    ): Store = localRepository.cacheRemoteStore(
        remoteRepository.createStore(
            CreateStoreRequest(
                name = name,
                type = type,
                address = address,
                phone = phone,
                businessHours = businessHours,
                minOrderAmount = minOrderAmount,
                deliveryFee = deliveryFee,
                isOpen = isOpen,
                pic = pic,
                tags = tags,
                categories = categories
            )
        )
    )
}
