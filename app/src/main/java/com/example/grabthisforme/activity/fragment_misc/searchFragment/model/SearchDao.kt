package com.example.grabthisforme.activity.fragment_misc.searchFragment.model
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Insert
    suspend fun insertSearchContent(search: SearchContent)

    @Query("SELECT * FROM search WHERE searchType = :type ORDER BY search_time DESC")
    fun getSearchByType(type: String): Flow<List<SearchContent>>

    @Query("DELETE FROM search WHERE searchType = :type")
    suspend fun clearByType(type: String)

    @Query("DELETE FROM search WHERE searchType = :type AND content = :targetContent")
    suspend fun deleteByTypeAndContent(type: String, targetContent: String)
    @Delete
    suspend fun deleteHistory(history: SearchContent)
}