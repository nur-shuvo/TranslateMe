package com.nurshuvo.translateme.database.dao

import androidx.room.*
import com.nurshuvo.translateme.database.entity.TranslationHistory

@Dao
interface TranslationHistoryDao {

    @Query("SELECT * FROM translation_history_table")
    suspend fun getAll(): List<TranslationHistory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(translationHistory: TranslationHistory)

    @Query("UPDATE translation_history_table SET isFavourite=:isFavourite WHERE fromText = :fromText")
    suspend fun makeItemFavourite(fromText: String, isFavourite: Boolean)

    @Delete()
    suspend fun deleteRow(translationHistory: TranslationHistory)

    @Query("DELETE FROM translation_history_table")
    suspend fun deleteAll()

    @Query("UPDATE translation_history_table  SET isFavourite=:newIsFavorite where isFavourite = :olDisFavorite")
    suspend fun undoAllFavoriteItems(olDisFavorite: Boolean, newIsFavorite: Boolean)
}