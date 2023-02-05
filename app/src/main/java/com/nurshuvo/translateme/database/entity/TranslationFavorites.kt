package com.nurshuvo.translateme.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_favorites_table")
data class TranslationFavorites(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "fromText") val fromText: String,
    @ColumnInfo(name = "translatedText") val translatedText: String
)
