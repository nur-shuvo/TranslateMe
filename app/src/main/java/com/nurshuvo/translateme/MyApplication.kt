package com.nurshuvo.translateme

import android.app.Application
import com.nurshuvo.translateme.data.repository.TranslationRepository
import com.nurshuvo.translateme.database.TranslationDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {
    // add application level task if needed
}