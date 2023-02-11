package com.nurshuvo.translateme.data.repository

import com.nurshuvo.translateme.data.database.dao.TranslationFavoritesDao
import com.nurshuvo.translateme.data.database.dao.TranslationHistoryDao
import com.nurshuvo.translateme.data.database.entity.TranslationFavorites
import com.nurshuvo.translateme.data.database.entity.TranslationHistory
import javax.inject.Inject

class TranslationRepository @Inject constructor(
    private val translationHistoryDao: TranslationHistoryDao,
    private val translationFavoritesDao: TranslationFavoritesDao
) {

    // Methods for handling History DB Table
    suspend fun getAllTranslationHistory(): List<TranslationHistory> {
        return translationHistoryDao.getAll()
    }
    suspend fun addToTranslationHistory(translationHistory: TranslationHistory) {
        translationHistoryDao.insert(translationHistory)
    }
    suspend fun deleteHistoryItem(translationHistory: TranslationHistory) {
        translationHistoryDao.deleteRow(translationHistory)
    }
    suspend fun deleteAllHistory() {
        translationHistoryDao.deleteAll()
    }

    // Methods for handling Favorite DB Table
    suspend fun getAllTranslationFavorites(): List<TranslationFavorites> {
        return translationFavoritesDao.getAll()
    }
    suspend fun addToTranslationFavorites(translationFavorites: TranslationFavorites) {
        translationFavoritesDao.insert(translationFavorites)
    }
    suspend fun deleteFavoriteItem(translationFavorites: TranslationFavorites) {
        translationFavoritesDao.deleteRow(translationFavorites)
    }
    suspend fun deleteAllFavorites() {
        translationFavoritesDao.deleteAll()
    }
}