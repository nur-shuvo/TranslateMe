package com.nurshuvo.translateme.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history_table")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "fromText") val fromText: String,
    @ColumnInfo(name = "translatedText") val translatedText: String
)
