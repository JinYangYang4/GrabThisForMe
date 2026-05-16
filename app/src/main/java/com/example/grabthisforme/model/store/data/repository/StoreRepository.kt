package com.example.grabthisforme.model.store.data.repository

import com.example.grabthisforme.model.store.data.dao.StoreDao
import com.example.grabthisforme.model.store.data.mock.StoreSampleData
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val storeDao: StoreDao,
    private val goodsRepository: GoodsRepository,
    private val userRepository: UserRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val currentUser: StateFlow<User?> = userRepository.currentUser
    val currentUserId: StateFlow<Long?> = userRepository.currentUserId

    private val sourceStores: StateFlow<List<Store>> = storeDao.getAllStores()
        .map { stores ->
            if (stores.isEmpty()) StoreSampleData.createVirtualStores() else stores
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = StoreSampleData.createVirtualStores()
        )

    val allStoreList: StateFlow<List<Store>> = combine(
        sourceStores,
        goodsRepository.allGoodsList
    ) { stores, allGoods ->
        stores.bindGoodsByStoreId(allGoods)
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = StoreSampleData.createVirtualStores()
    )

    val myStoreList: StateFlow<List<Store>> = combine(
        allStoreList,
        userRepository.currentUserId
    ) { stores, currentUserId ->
        if (currentUserId == null) {
            emptyList()
        } else {
            stores.filter { it.ownerId == currentUserId }
        }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    suspend fun saveStore(store: Store) {
        storeDao.saveStore(store)
    }

    suspend fun updateStore(store: Store) {
        storeDao.saveStore(store)
    }

    suspend fun saveStores(stores: List<Store>) {
        storeDao.saveStores(stores)
    }

    suspend fun deleteStore(storeId: Long) {
        storeDao.deleteById(storeId)
    }

    fun getStoreFlow(storeId: Long): Flow<Store?> {
        return allStoreList.map { stores ->
            stores.firstOrNull { it.id == storeId }
        }
    }

    suspend fun getStore(storeId: Long): Store? {
        return getStoreFlow(storeId).first()
    }

    suspend fun registerStore(
        name: String,
        type: String,
        address: String,
        categories: List<String> = emptyList()
    ): Store {
        val currentUser = userRepository.currentUser.value
        val now = System.currentTimeMillis()
        val store = Store(
            id = now,
            name = name.trim(),
            type = type.trim(),
            address = address.trim(),
            ownerId = currentUser?.id ?: 0L,
            category = categories,
            phone = null,
            businessHours = null,
            pic = currentUser?.headPic?.takeIf { it.isNotBlank() },
            salesVolume = 0
        )
        storeDao.saveStore(store)
        return store
    }

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
    ): Store {
        val currentUser = userRepository.currentUser.value
        val now = System.currentTimeMillis()
        val store = Store(
            id = now,
            name = name.trim(),
            type = type.trim(),
            address = address.trim(),
            ownerId = currentUser?.id ?: 0L,
            phone = phone?.trim()?.takeIf { it.isNotBlank() },
            businessHours = businessHours?.trim()?.takeIf { it.isNotBlank() },
            minOrderAmount = minOrderAmount,
            deliveryFee = deliveryFee,
            isOpen = isOpen,
            pic = pic?.trim()?.takeIf { it.isNotBlank() } ?: currentUser?.headPic?.takeIf { it.isNotBlank() },
            tags = tags,
            category = categories,
            salesVolume = 0
        )
        storeDao.saveStore(store)
        return store
    }

    private fun List<Store>.bindGoodsByStoreId(allGoods: List<Goods>): List<Store> {
        if (isEmpty()) return this
        if (allGoods.isEmpty()) return this
        return map { store ->
            val storeGoods = allGoods.filter { it.storeId == store.id }
            if (storeGoods.isEmpty()) {
                store
            } else {
                store.withGoods(storeGoods)
            }
        }
    }
}
