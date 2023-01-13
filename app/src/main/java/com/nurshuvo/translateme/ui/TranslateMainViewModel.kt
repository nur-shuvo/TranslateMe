package com.nurshuvo.translateme.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurshuvo.translateme.network.Api
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import java.lang.Exception
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val TAG = "TranslateMainViewModel"

class TranslateMainViewModel : ViewModel() {

    private val _translatedText = MutableLiveData<String>()
    val translatedText: LiveData<String>
        get() = _translatedText

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