package com.nurshuvo.translateme.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.nurshuvo.translateme.R

private const val TAG = "TranslateMainActivity"

class TranslateMainActivity : AppCompatActivity() {

    private val viewModel: TranslateMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate_main)

        initObserver()
        initializeBengaliEnglishModel()

        // Button click for translation
        findViewById<Button>(R.id.btn).setOnClickListener {
            val fromText = findViewById<EditText>(R.id.edtText).text.toString()

            // API call later when app is ready, as there is a API limit for now.
            // viewModel.translateToEnglish(fromText)

            // Translate by local ML model
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.BENGALI)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            val bengaliEnglishTranslator = Translation.getClient(options)
            bengaliEnglishTranslator.translate(fromText)
                .addOnSuccessListener { translatedText ->
                    Log.i(TAG, "Translation successful")
                    findViewById<TextView>(R.id.txtVwOutput).text = translatedText
                }
                .addOnFailureListener {
                    Log.i(TAG, "Translation error")
                }
        }
    }

    private fun initObserver() {
        viewModel.translatedText.observe(this) { value ->
            findViewById<TextView>(R.id.txtVwOutput).text = value
        }
    }

    private fun initializeBengaliEnglishModel() {
        val modelManager = RemoteModelManager.getInstance()

        // Get translation models stored on the device.
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener { models ->
                models.forEach {
                    Log.i(TAG, " ${it.language} ")
                    if (it.language == TranslateLanguage.BENGALI || it.language == TranslateLanguage.ENGLISH) {
                        Log.e(TAG, "Already model installed")
                    } else {
                        Toast.makeText(this, "Model download started!", Toast.LENGTH_LONG).show()
                        fetchBengaliEnglishModel()
                    }
                }
            }
            .addOnFailureListener {
                // Error
                Log.e(TAG, "Download error")
            }
    }

    private fun fetchBengaliEnglishModel() {
        val modelManager = RemoteModelManager.getInstance()
        // Download the BengaliEnglish model.
        val bengaliModel = TranslateRemoteModel.Builder(TranslateLanguage.BENGALI).build()
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        modelManager.download(bengaliModel, conditions)
            .addOnSuccessListener {
                // Model downloaded.
            }
            .addOnFailureListener {
                // Error.
            }
    }
}

