package com.nurshuvo.translateme.data.database.dao

import androidx.room.*
import com.nurshuvo.translateme.data.database.entity.TranslationHistory

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