package com.nurshuvo.translateme.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nurshuvo.translateme.database.dao.TranslationHistoryDao
import com.nurshuvo.translateme.database.entity.TranslationHistory

// Annotates class to be a Room Database with a table (entity) of the TranslationHistory class
@Database(entities = [TranslationHistory::class], version = 1, exportSchema = false)
public abstract class TranslationDatabase : RoomDatabase() {

    abstract fun translationHistoryDao(): TranslationHistoryDao

    companion object {
        const val DATABASE_NAME = "word_database"
    }
}
