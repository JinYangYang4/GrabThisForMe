package com.example.grabthisforme.model.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UserDao {

    @Transaction
    suspend fun loginAndSetCurrent(user: User) {
        resetAllCurrent()
        val newUser = user.copy(isCurrent = true)
        saveUser(newUser)
    }

    @Query("SELECT * FROM user WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("UPDATE user SET isCurrent = 0 WHERE isCurrent = 1")
    suspend fun logoutCurrentUser()

    @Query("SELECT * FROM user")
    suspend fun getAllLoginUsers(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUser(user: User)

    @Query("UPDATE user SET isCurrent = 0")
    suspend fun resetAllCurrent()

    @Query("DELETE FROM user WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)
}