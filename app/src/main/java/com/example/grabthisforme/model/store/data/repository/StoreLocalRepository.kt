package com.example.grabthisforme.model.store.data.repository

import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.relation.data.dao.StoreRelationDao
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryEntity
import com.example.grabthisforme.model.relation.data.entity.StoreGoodsCategoryItemEntity
import com.example.grabthisforme.model.relation.data.entity.StoreTagEntity
import com.example.grabthisforme.model.store.data.local.dao.StoreDao
import com.example.grabthisforme.model.store.data.mock.StoreSampleData
import com.example.grabthisforme.model.store.domain.Store
import com.example.grabthisforme.model.store.mapper.toDomain
import com.example.grabthisforme.model.store.mapper.toEntity
import com.example.grabthisforme.model.user.data.repository.UserRepository
import com.example.grabthisforme.model.user.domain.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class StoreLocalRepository @Inject constructor(
    private val storeDao: StoreDao,
    private val storeRelationDao: StoreRelationDao,
    private val goodsRepository: GoodsRepository,
    private val userRepository: UserRepository
) {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val currentUser: StateFlow<User?> = userRepository.currentUser
    val currentUserId: StateFlow<Long?> = userRepository.currentUserId

    private val sourceStores: StateFlow<List<Store>> = storeDao.getAllStoreEntitiesFlow()
        .map { entities ->
            if (entities.isEmpty()) {
                StoreSampleData.createVirtualStores()
            } else {
                entities.map { it.toDomain() }
            }
        }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.Eagerly,
            initialValue = StoreSampleData.createVirtualStores()
        )

    val allStoreList: StateFlow<List<Store>> = combine(
        sourceStores,
        storeRelationDao.observeAllTags()
    ) { stores, tags ->
        injectTags(stores, tags)
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
        storeDao.upsert(store.toEntity())
        syncTags(store.id, store.tags)
    }

    suspend fun updateStore(store: Store) {
        storeDao.upsert(store.toEntity())
        syncTags(store.id, store.tags)
    }

    suspend fun saveStores(stores: List<Store>) {
        storeDao.upsertAll(stores.map { it.toEntity() })
        stores.forEach { store ->
            syncTags(store.id, store.tags)
        }
    }

    suspend fun deleteStore(storeId: Long) {
        storeRelationDao.deleteAllCategoryItemsByStoreId(storeId)
        storeRelationDao.deleteCategoriesByStoreId(storeId)
        storeRelationDao.deleteTagsByStoreId(storeId)
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

    fun observeStoreCategories(storeId: Long): Flow<List<String>> {
        if (storeId <= 0L) {
            return flowOf(ensureBaseCategories(emptyList()))
        }
        return storeRelationDao.observeCategoriesByStoreId(storeId)
            .map { entities ->
                val categories = entities
                    .sortedBy { it.sortOrder }
                    .map { it.category.trim() }
                    .filter { it.isNotBlank() }
                ensureBaseCategories(categories)
            }
    }

    fun observeGoodsByStoreAndCategory(storeId: Long, category: String): Flow<List<Goods>> {
        if (storeId <= 0L) {
            return flowOf(emptyList())
        }
        return combine(
            goodsRepository.getGoodsByStoreId(storeId),
            storeRelationDao.observeCategoriesByStoreId(storeId),
            storeRelationDao.observeCategoryItemsByStoreId(storeId)
        ) { goods, categories, items ->
            filterGoodsByCategory(goods, category, categories, items)
        }
    }

    fun observeGoodsOutsideSelectedCategory(storeId: Long, category: String): Flow<List<Goods>> {
        if (storeId <= 0L) {
            return flowOf(emptyList())
        }
        return combine(
            goodsRepository.getGoodsByStoreId(storeId),
            storeRelationDao.observeCategoriesByStoreId(storeId),
            storeRelationDao.observeCategoryItemsByStoreId(storeId)
        ) { goods, categories, items ->
            filterUnselectGoods(goods, category, categories, items)
        }
    }

    suspend fun updateStoreCategoriesOnly(
        storeId: Long,
        categories: List<String>,
        renamedCategories: Map<String, String> = emptyMap()
    ) {
        val existingCategories = storeRelationDao.getCategoriesByStoreId(storeId).sortedBy { it.sortOrder }
        val normalizedCategories = ensureBaseCategories(categories)
        val items = storeRelationDao.getCategoryItemsByStoreId(storeId)
        val updatedMemberships = buildCustomMemberships(existingCategories, items)
            .mapValues { (_, categorySet) ->
                categorySet.map { categoryName ->
                    renamedCategories[categoryName]?.trim().orEmpty().ifBlank { categoryName }
                }.filter { it in normalizedCategories }.toSet()
            }
        rewriteCategoryRelations(
            storeId = storeId,
            suppliesGoods = getAllGoodsByStoreId(storeId),
            categories = normalizedCategories,
            membershipsByGoodsId = updatedMemberships
        )
    }

    suspend fun assignGoodsToCategory(storeId: Long, goodsId: Long, category: String) {
        val normalizedCategory = category.trim()
        if (
            normalizedCategory.isBlank() ||
            normalizedCategory == Store.CATEGORY_ALL ||
            normalizedCategory == Store.CATEGORY_UNCLASSIFIED
        ) {
            return
        }
        val existingCategories = storeRelationDao.getCategoriesByStoreId(storeId).sortedBy { it.sortOrder }
        val items = storeRelationDao.getCategoryItemsByStoreId(storeId)
        val updatedMemberships = buildCustomMemberships(existingCategories, items).toMutableMap()
        val assignments = updatedMemberships.getOrPut(goodsId) { linkedSetOf() }
        assignments.add(normalizedCategory)
        rewriteCategoryRelations(
            storeId = storeId,
            suppliesGoods = getAllGoodsByStoreId(storeId),
            categories = ensureBaseCategories(existingCategories.map { it.category } + normalizedCategory),
            membershipsByGoodsId = updatedMemberships
        )
    }

    suspend fun moveGoodsToUnclassified(storeId: Long, goodsId: Long) {
        val existingCategories = storeRelationDao.getCategoriesByStoreId(storeId).sortedBy { it.sortOrder }
        val items = storeRelationDao.getCategoryItemsByStoreId(storeId)
        val updatedMemberships = buildCustomMemberships(existingCategories, items).toMutableMap()
        updatedMemberships[goodsId] = linkedSetOf()
        rewriteCategoryRelations(
            storeId = storeId,
            suppliesGoods = getAllGoodsByStoreId(storeId),
            categories = ensureBaseCategories(existingCategories.map { it.category }),
            membershipsByGoodsId = updatedMemberships
        )
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
            identity = com.example.grabthisforme.model.store.domain.StoreIdentity(
                id = now,
                name = name.trim(),
                type = type.trim(),
                ownerId = currentUser?.id ?: 0L
            ),
            location = com.example.grabthisforme.model.store.domain.StoreLocation(
                address = address.trim()
            ),
            commercialInfo = com.example.grabthisforme.model.store.domain.StoreCommercialInfo(
                phone = null,
                businessHours = null,
                pic = currentUser?.headPic?.takeIf { it.isNotBlank() }
            ),
            statistics = com.example.grabthisforme.model.store.domain.StoreStatistics(salesVolume = 0)
        )
        saveStore(store)
        if (categories.isNotEmpty()) {
            syncCategories(store.id, categories)
        }
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
            identity = com.example.grabthisforme.model.store.domain.StoreIdentity(
                id = now,
                name = name.trim(),
                type = type.trim(),
                ownerId = currentUser?.id ?: 0L
            ),
            location = com.example.grabthisforme.model.store.domain.StoreLocation(
                address = address.trim()
            ),
            commercialInfo = com.example.grabthisforme.model.store.domain.StoreCommercialInfo(
                phone = phone?.trim()?.takeIf { it.isNotBlank() },
                businessHours = businessHours?.trim()?.takeIf { it.isNotBlank() },
                minOrderAmount = minOrderAmount,
                deliveryFee = deliveryFee,
                isOpen = isOpen,
                pic = pic?.trim()?.takeIf { it.isNotBlank() } ?: currentUser?.headPic?.takeIf { it.isNotBlank() },
                tags = tags
            ),
            statistics = com.example.grabthisforme.model.store.domain.StoreStatistics(salesVolume = 0)
        )
        saveStore(store)
        if (categories.isNotEmpty()) {
            syncCategories(store.id, categories)
        }
        return store
    }

    private suspend fun syncCategories(storeId: Long, categories: List<String>) {
        val normalized = ensureBaseCategories(categories)
        val categoryEntities = normalized.mapIndexed { index, category ->
            StoreGoodsCategoryEntity(
                groupId = storeId * 1000 + index + 1,
                storeId = storeId,
                category = category,
                sortOrder = index
            )
        }
        storeRelationDao.replaceGoodsCategories(storeId = storeId, categories = categoryEntities, items = emptyList())
    }

    private suspend fun syncTags(storeId: Long, tags: List<String>) {
        val tagEntities = normalizeTags(tags).mapIndexed { index, tag ->
            StoreTagEntity(storeId = storeId, tag = tag, sortOrder = index)
        }
        storeRelationDao.replaceTags(storeId, tagEntities)
    }

    private fun normalizeTags(tags: List<String>): List<String> {
        return tags.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    private fun ensureBaseCategories(categories: List<String>): List<String> {
        val normalizedCustomCategories = categories
            .map { it.trim() }
            .filter {
                it.isNotBlank() &&
                    it != Store.CATEGORY_ALL &&
                    it != Store.CATEGORY_UNCLASSIFIED
            }
            .distinct()
        return buildList {
            add(Store.CATEGORY_ALL)
            add(Store.CATEGORY_UNCLASSIFIED)
            addAll(normalizedCustomCategories)
        }
    }

    private suspend fun getAllGoodsByStoreId(storeId: Long): List<Goods> {
        return try {
            goodsRepository.getGoodsByStoreId(storeId).first()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun buildCustomMemberships(
        categories: List<StoreGoodsCategoryEntity>,
        items: List<StoreGoodsCategoryItemEntity>
    ): Map<Long, LinkedHashSet<String>> {
        val customCategoryNameByGroupId = categories
            .associate { it.groupId to it.category.trim() }
            .filterValues { categoryName ->
                categoryName.isNotBlank() &&
                    categoryName != Store.CATEGORY_ALL &&
                    categoryName != Store.CATEGORY_UNCLASSIFIED
            }
        val memberships = linkedMapOf<Long, LinkedHashSet<String>>()
        items.forEach { item ->
            val categoryName = customCategoryNameByGroupId[item.groupId] ?: return@forEach
            memberships.getOrPut(item.goodsId) { linkedSetOf() }.add(categoryName)
        }
        return memberships
    }

    private fun filterGoodsByCategory(
        goods: List<Goods>,
        selectedCategory: String,
        categories: List<StoreGoodsCategoryEntity>,
        items: List<StoreGoodsCategoryItemEntity>
    ): List<Goods> {
        val normalizedCategory = selectedCategory.trim().ifBlank { Store.CATEGORY_ALL }
        if (normalizedCategory == Store.CATEGORY_ALL) return goods
        val membershipsByGoodsId = buildCustomMemberships(categories, items)
        return when (normalizedCategory) {
            Store.CATEGORY_UNCLASSIFIED -> goods.filter { membershipsByGoodsId[it.id].isNullOrEmpty() }
            else -> goods.filter { normalizedCategory in membershipsByGoodsId[it.id].orEmpty() }
        }
    }

    private fun filterUnselectGoods(
        goods: List<Goods>,
        selectedCategory: String,
        categories: List<StoreGoodsCategoryEntity>,
        items: List<StoreGoodsCategoryItemEntity>
    ): List<Goods> {
        val normalizedCategory = selectedCategory.trim()
        if (normalizedCategory.isBlank() || normalizedCategory == Store.CATEGORY_ALL) return emptyList()
        val membershipsByGoodsId = buildCustomMemberships(categories, items)
        return if (normalizedCategory == Store.CATEGORY_UNCLASSIFIED) {
            goods.filter { membershipsByGoodsId[it.id].orEmpty().isNotEmpty() }
        } else {
            goods.filter { normalizedCategory !in membershipsByGoodsId[it.id].orEmpty() }
        }
    }

    private suspend fun rewriteCategoryRelations(
        storeId: Long,
        suppliesGoods: List<Goods>,
        categories: List<String>,
        membershipsByGoodsId: Map<Long, Set<String>>
    ) {
        val uniqueGoods = suppliesGoods.distinctBy { it.id }
        val normalizedCategories = ensureBaseCategories(categories)
        val categoryEntities = normalizedCategories.mapIndexed { index, category ->
            StoreGoodsCategoryEntity(
                groupId = storeId * 1000 + index + 1,
                storeId = storeId,
                category = category,
                sortOrder = index
            )
        }
        val categoryIdByName = categoryEntities.associate { it.category to it.groupId }
        val itemEntities = mutableListOf<StoreGoodsCategoryItemEntity>()

        categoryIdByName[Store.CATEGORY_ALL]?.let { groupId ->
            uniqueGoods.forEachIndexed { index, goods ->
                itemEntities += StoreGoodsCategoryItemEntity(groupId = groupId, goodsId = goods.id, sortOrder = index)
            }
        }

        uniqueGoods.forEach { goods ->
            val assignments = membershipsByGoodsId[goods.id]
                .orEmpty()
                .map { it.trim() }
                .filter {
                    it.isNotBlank() &&
                        it != Store.CATEGORY_ALL &&
                        it != Store.CATEGORY_UNCLASSIFIED &&
                        categoryIdByName.containsKey(it)
                }
                .distinct()
            if (assignments.isEmpty()) {
                val groupId = categoryIdByName[Store.CATEGORY_UNCLASSIFIED] ?: return@forEach
                val sortOrder = itemEntities.count { it.groupId == groupId }
                itemEntities += StoreGoodsCategoryItemEntity(groupId = groupId, goodsId = goods.id, sortOrder = sortOrder)
            } else {
                assignments.forEach { categoryName ->
                    val groupId = categoryIdByName[categoryName] ?: return@forEach
                    val sortOrder = itemEntities.count { it.groupId == groupId }
                    itemEntities += StoreGoodsCategoryItemEntity(groupId = groupId, goodsId = goods.id, sortOrder = sortOrder)
                }
            }
        }

        storeRelationDao.replaceGoodsCategories(storeId = storeId, categories = categoryEntities, items = itemEntities)
    }

    private fun injectTags(stores: List<Store>, tags: List<StoreTagEntity>): List<Store> {
        if (stores.isEmpty()) return stores
        val tagsByStoreId = tags.groupBy { it.storeId }
        return stores.map { store ->
            val storeTags = tagsByStoreId[store.id]?.sortedBy { it.sortOrder }?.map { it.tag } ?: store.tags
            store.copy(commercialInfo = store.commercialInfo.copy(tags = storeTags))
        }
    }
}
