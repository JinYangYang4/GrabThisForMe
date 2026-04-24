package com.example.grabthisforme.model.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)  //怎么判断
    suspend fun upsertAccount(account: UserAccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Transaction
    suspend fun saveUser(user: User) {
        upsertAccount(user.toAccountEntity())
        upsertProfile(user.toProfileEntity())
    }

    @Transaction
    suspend fun loginAndSetCurrent(user: User) {
        resetAllCurrent()
        saveUser(user.withCurrent(true))
    }

    @Transaction
    @Query("SELECT * FROM user_account WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUserBundle(): UserBundleEntity?

    suspend fun getCurrentUser(): User? {
        return getCurrentUserBundle()?.toDomain()
    }

    @Query("UPDATE user_account SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun logoutCurrentUser()

    @Transaction
    @Query("SELECT * FROM user_account ORDER BY isCurrent DESC, createTime DESC")
    suspend fun getAllLoginUserBundles(): List<UserBundleEntity>

    suspend fun getAllLoginUsers(): List<User> {
        return getAllLoginUserBundles().map { it.toDomain() }
    }

    @Query("UPDATE user_account SET isCurrent = 0")
    suspend fun resetAllCurrent()

    @Query("DELETE FROM user_account WHERE userId = :userId")
    suspend fun deleteUserById(userId: Long)
}

