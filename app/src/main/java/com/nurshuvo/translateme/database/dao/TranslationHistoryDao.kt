package com.nurshuvo.translateme.database.dao

import androidx.room.*
import com.nurshuvo.translateme.database.entity.TranslationHistory

@Dao
interface TranslationHistoryDao {

    @Query("SELECT * FROM translation_history_table")
    suspend fun getAll(): List<TranslationHistory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(translationHistory: TranslationHistory)

    @Delete()
    suspend fun deleteRow(translationHistory: TranslationHistory)

    @Query("DELETE FROM translation_history_table")
    suspend fun deleteAll()
}