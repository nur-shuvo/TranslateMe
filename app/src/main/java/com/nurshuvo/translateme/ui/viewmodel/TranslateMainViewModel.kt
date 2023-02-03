package com.nurshuvo.translateme.ui.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.nurshuvo.translateme.data.repository.TranslationRepository
import com.nurshuvo.translateme.database.entity.TranslationHistory
import com.nurshuvo.translateme.network.Api
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.lang.Exception
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

private const val TAG = "TranslateMainViewModel"

@HiltViewModel
class TranslateMainViewModel @Inject constructor(
    private val translationRepository: TranslationRepository
) : ViewModel() {

    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String>
        get() = _translatedText

    var isBengaliToEnglish = true

    suspend fun addToTranslationHistory(translationHistory: TranslationHistory) {
        translationRepository.addToTranslationHistory(translationHistory)
    }

    suspend fun updateTranslationHistory(fromText: String) {
        translationRepository.makeItemFavourite(fromText)
    }

    suspend fun undoFavoriteFromItems(fromText: String) {
        translationRepository.undoItemFavourite(fromText)
    }

    suspend fun getAllTranslationHistory(): List<TranslationHistory> {
        return translationRepository.getAllTranslationHistory()
    }

    fun translateToEnglish(fromText: String) {
        viewModelScope.launch {
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val who = URLEncoder.encode(fromText, StandardCharsets.UTF_8.toString())
            val body = RequestBody.create(mediaType, "q=$who&target=en&source=bn")
            val retObject = Api.retrofitService.translate(body)
            try {
                _translatedText.value = retObject.data.translations[0].translatedText
            } catch (e: Exception) {
                Log.e(TAG, "exception: translateToEnglish")
            }
        }
    }
}