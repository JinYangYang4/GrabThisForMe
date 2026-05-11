package com.example.grabthisforme.model.store.data.repository

import com.example.grabthisforme.model.store.data.dao.StoreDao
import com.example.grabthisforme.model.store.data.mock.StoreSampleData
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val storeDao: StoreDao,
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

    val allStoreList: StateFlow<List<Store>> = sourceStores

    val myStoreList: StateFlow<List<Store>> = combine(
        sourceStores,
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

    suspend fun saveStores(stores: List<Store>) {
        storeDao.saveStores(stores)
    }

    suspend fun deleteStore(storeId: Long) {
        storeDao.deleteById(storeId)
    }

    suspend fun registerStore(
        name: String,
        type: String,
        address: String
    ): Store {
        val currentUser = userRepository.currentUser.value
        val now = System.currentTimeMillis()
        val store = Store(
            id = now,
            name = name.trim(),
            type = type.trim(),
            address = address.trim(),
            ownerId = currentUser?.id ?: 0L,
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
        tags: List<String>
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
            salesVolume = 0
        )
        storeDao.saveStore(store)
        return store
    }
}
