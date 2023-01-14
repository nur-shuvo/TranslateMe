package com.nurshuvo.translateme.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nurshuvo.translateme.database.dao.TranslationHistoryDao
import com.nurshuvo.translateme.database.entity.TranslationHistory

// Annotates class to be a Room Database with a table (entity) of the TranslationHistory class
@Database(entities = [TranslationHistory::class], version = 1, exportSchema = false)
public abstract class TranslationDatabase : RoomDatabase() {

    abstract fun translationHistoryDao(): TranslationHistoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TranslationDatabase? = null

        fun getDatabase(context: Context): TranslationDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TranslationDatabase::class.java,
                    "word_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
