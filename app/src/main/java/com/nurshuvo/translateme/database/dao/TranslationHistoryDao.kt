package com.nurshuvo.translateme.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nurshuvo.translateme.database.entity.TranslationHistory

@Dao
interface TranslationHistoryDao {

    @Query("SELECT * FROM translation_history_table")
    suspend fun getAll(): List<TranslationHistory>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(translationHistory: TranslationHistory)

    @Query("DELETE FROM translation_history_table")
    suspend fun deleteAll()
}