package com.example.grabthisforme.model.user.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.grabthisforme.model.user.data.local.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.local.entity.UserBasicBundleEntity
import com.example.grabthisforme.model.user.data.local.entity.UserBundleEntity
import com.example.grabthisforme.model.user.data.local.entity.UserProfileEntity
import com.example.grabthisforme.model.user.data.local.entity.UserStatisticsEntity
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toAccountEntity
import com.example.grabthisforme.model.user.mapper.toDomain
import com.example.grabthisforme.model.user.mapper.toProfileEntity
import com.example.grabthisforme.model.user.mapper.toStatisticsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertAccount(account: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatistics(statistics: UserStatisticsEntity)

    @Transaction
    suspend fun saveUser(user: User) {
        upsertAccount(user.toAccountEntity())
        upsertProfile(user.toProfileEntity())
        upsertStatistics(user.toStatisticsEntity())
    }

    @Transaction
    suspend fun loginAndSetCurrent(user: User) {
        resetAllCurrent()
        saveUser(user.withCurrent(true))
    }

    @Transaction
    @Query("SELECT * FROM user_account WHERE isCurrent = 1 AND isLoginAccount = 1 LIMIT 1")
    fun getCurrentUserBundleFlow(): Flow<UserBundleEntity?>

    fun getCurrentUser(): Flow<User?> {
        return getCurrentUserBundleFlow().map { it?.toDomain() }
    }

    @Query("UPDATE user_account SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun logoutCurrentUser()

    @Transaction
    @Query("SELECT * FROM user_account WHERE isLoginAccount = 1 ORDER BY isCurrent DESC, createTime DESC")
    fun getAllLoginUserBundlesFlow(): Flow<List<UserBundleEntity>>

    @Transaction
    @Query("SELECT * FROM user_account WHERE userId IN (:userIds)")
    suspend fun getUserBasicBundlesByIds(userIds: List<Long>): List<UserBasicBundleEntity>

    @Transaction
    @Query("SELECT * FROM user_account WHERE userId IN (:userIds)")
    fun observeUserBasicBundlesByIds(userIds: List<Long>): Flow<List<UserBasicBundleEntity>>

    @Transaction
    @Query("SELECT * FROM user_account ORDER BY isCurrent DESC, createTime DESC")
    fun observeAllUserBasicBundles(): Flow<List<UserBasicBundleEntity>>

    fun getAllLoginUsers(): Flow<List<User>> {
        return getAllLoginUserBundlesFlow().map { bundles ->
            bundles.map { it.toDomain() }
        }
    }

    @Query("UPDATE user_account SET isCurrent = 0")
    suspend fun resetAllCurrent()

    @Query("DELETE FROM user_account WHERE userId = :userId")
    suspend fun deleteUserById(userId: Long)

    @Query("DELETE FROM user_account WHERE userId IN (:userIds)")
    suspend fun deleteUsersByIds(userIds: List<Long>)
}
