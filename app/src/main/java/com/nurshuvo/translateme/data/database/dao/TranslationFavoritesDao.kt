package com.nurshuvo.translateme.data.database.dao

import androidx.room.*
import com.nurshuvo.translateme.data.database.entity.TranslationFavorites
import com.nurshuvo.translateme.data.database.entity.TranslationHistory

@Dao
interface TranslationFavoritesDao {
    @Query("SELECT * FROM translation_favorites_table")
    suspend fun getAll(): List<TranslationFavorites>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(translationHistory: TranslationFavorites)

    @Delete()
    suspend fun deleteRow(translationHistory: TranslationFavorites)

    @Query("DELETE FROM translation_favorites_table")
    suspend fun deleteAll()
}