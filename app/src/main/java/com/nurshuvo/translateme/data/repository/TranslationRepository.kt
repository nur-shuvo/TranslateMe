package com.nurshuvo.translateme.data.repository

import com.nurshuvo.translateme.database.dao.TranslationHistoryDao
import com.nurshuvo.translateme.database.entity.TranslationHistory

class TranslationRepository(private val translationHistoryDao: TranslationHistoryDao) {
    suspend fun getAllTranslationHistory(): List<TranslationHistory> {
        return translationHistoryDao.getAll()
    }
    suspend fun addToTranslationHistory(translationHistory: TranslationHistory) {
        translationHistoryDao.insert(translationHistory)
    }
    suspend fun makeItemFavourite(fromText: String) {
        translationHistoryDao.makeItemFavourite(fromText, true)
    }
    suspend fun undoItemFavourite(fromText: String) {
        translationHistoryDao.makeItemFavourite(fromText, false)
    }
    suspend fun deleteHistoryItem(translationHistory: TranslationHistory) {
        translationHistoryDao.deleteRow(translationHistory)
    }
    suspend fun deleteAll() {
        translationHistoryDao.deleteAll()
    }
    suspend fun unDoAllFavoriteRecords() {
        translationHistoryDao.undoAllFavoriteItems(true, false)
    }
}