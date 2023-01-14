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
}