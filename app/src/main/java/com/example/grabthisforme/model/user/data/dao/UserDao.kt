package com.example.grabthisforme.model.user.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.grabthisforme.model.user.data.entity.UserAccountEntity
import com.example.grabthisforme.model.user.data.entity.UserBundleEntity
import com.example.grabthisforme.model.user.data.entity.UserLikeEntity
import com.example.grabthisforme.model.user.data.entity.UserProfileEntity
import com.example.grabthisforme.model.user.data.entity.UserStatisticsEntity
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.model.user.mapper.toAccountEntity
import com.example.grabthisforme.model.user.mapper.toDomain
import com.example.grabthisforme.model.user.mapper.toLikeEntity
import com.example.grabthisforme.model.user.mapper.toProfileEntity
import com.example.grabthisforme.model.user.mapper.toStatisticsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAccount(account: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStatistics(statistics: UserStatisticsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLike(like: UserLikeEntity)

    @Transaction
    suspend fun saveUser(user: User) {
        upsertAccount(user.toAccountEntity())
        upsertProfile(user.toProfileEntity())
        upsertStatistics(user.toStatisticsEntity())
        upsertLike(user.toLikeEntity())
    }

    @Transaction
    suspend fun loginAndSetCurrent(user: User) {
        resetAllCurrent()
        saveUser(user.withCurrent(true))
    }

    @Transaction
    @Query("SELECT * FROM user_account WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUserBundleFlow(): Flow<UserBundleEntity?>

    fun getCurrentUser(): Flow<User?> {
        return getCurrentUserBundleFlow().map { it?.toDomain() }
    }

    @Query("UPDATE user_account SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun logoutCurrentUser()

    @Transaction
    @Query("SELECT * FROM user_account ORDER BY isCurrent DESC, createTime DESC")
    fun getAllLoginUserBundlesFlow(): Flow<List<UserBundleEntity>>

    fun getAllLoginUsers(): Flow<List<User>> {
        return getAllLoginUserBundlesFlow().map { bundles ->
            bundles.map { it.toDomain() }
        }
    }

    @Query("UPDATE user_account SET isCurrent = 0")
    suspend fun resetAllCurrent()

    @Query("DELETE FROM user_account WHERE userId = :userId")
    suspend fun deleteUserById(userId: Long)
}
