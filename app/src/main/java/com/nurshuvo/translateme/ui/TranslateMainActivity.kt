package com.nurshuvo.translateme.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.nurshuvo.translateme.MyApplication
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.database.entity.TranslationHistory
import com.nurshuvo.translateme.ui.viewmodel.TranslateMainViewModel
import com.nurshuvo.translateme.ui.viewmodel.TranslateMainViewModelFactory
import com.nurshuvo.translateme.util.TranslationObject
import kotlinx.coroutines.launch

private const val TAG = "TranslateMainActivity"

class TranslateMainActivity : AppCompatActivity() {

    private val viewModel: TranslateMainViewModel by viewModels {
        TranslateMainViewModelFactory((application as MyApplication).translationRepository)
    }

    private var drawerLayout: DrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var navigationView: NavigationView? = null
    private var inputTextEditText: EditText? = null
    private var translateButton: ImageView? = null
    private var outputTextView: TextView? = null
    private var copyButton: ImageView? = null
    private var favButton: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        drawerLayout = findViewById(R.id.my_drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        inputTextEditText = findViewById(R.id.edtText)
        translateButton = findViewById(R.id.btn)
        outputTextView = findViewById(R.id.txtVwOutput)
        copyButton = findViewById(R.id.copy_icon)
        favButton = findViewById(R.id.fav_icon)

        setUpNavDrawer()
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu_icon_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setOnclickItemsOnNavigationDrawer()
        setOnClickListenerForTranslateButton()
        setOnClickListenerForCopyButton()
        setOnCLickOnFavButton()
        initObserver()
        // initializeBengaliEnglishModel()
    }

    private fun setOnCLickOnFavButton() {
        favButton?.setOnClickListener {
            Log.i(TAG, "Favourite button clicked!")
            if (outputTextView?.text.isNullOrBlank() || inputTextEditText?.text.isNullOrBlank()) {
                return@setOnClickListener
            }

            Toast.makeText(this@TranslateMainActivity, "Added to Favourites!", Toast.LENGTH_SHORT)
                .show()
            // update existing records in DB as favourite = true in history table
            lifecycleScope.launch {
                viewModel.updateTranslationHistory(inputTextEditText?.text.toString())
            }
        }
    }

    private fun setOnClickListenerForCopyButton() {
        copyButton?.setOnClickListener {
            Log.i(TAG, "Clip board button clicked!")
            if (outputTextView?.text.isNullOrBlank()) {
                return@setOnClickListener
            }
            // Gets a handle to the clipboard service.
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("simple text", outputTextView?.text)
            clipboard.setPrimaryClip(clip)
        }
    }

    private fun setOnClickListenerForTranslateButton() {
        translateButton?.setOnClickListener {
            Log.i(TAG, "Translation button clicked!")
            val fromText = inputTextEditText?.text.toString()

            if (fromText.isBlank()) {
                return@setOnClickListener
            }

            // API call later when app is ready, as there is a API limit for now.
            // viewModel.translateToEnglish(fromText)

            // Translate by local ML model
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.BENGALI)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()
            val bengaliEnglishTranslator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
            bengaliEnglishTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    bengaliEnglishTranslator.translate(fromText)
                        .addOnSuccessListener { translatedText ->
                            Log.i(TAG, "Translation successful")
                            outputTextView?.text = translatedText
                            lifecycleScope.launch {
                                viewModel.addToTranslationHistory(
                                    TranslationHistory(0, fromText, translatedText)
                                )
                            }
                        }
                        .addOnFailureListener {
                            Log.i(TAG, "Translation error")
                            // Model could n’t be downloaded or other internal error.
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "$exception")
                }
        }
    }

    private fun setUpNavDrawer() {
        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout?.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle?.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle?.onOptionsItemSelected(item) == true) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun setOnclickItemsOnNavigationDrawer() {
        navigationView?.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.history -> {
                    startActivity(
                        Intent("com.nurshuvo.translateme.history")
                            .putExtra("key_isFavoriteListRequested", false)
                    )
                }
                R.id.favorite -> {
                    startActivity(
                        Intent("com.nurshuvo.translateme.history")
                            .putExtra("key_isFavoriteListRequested", true)
                    )
                }
                R.id.feedback -> {
                    // TODO
                }
            }
            drawerLayout?.closeDrawer(GravityCompat.START)
            true
        }
    }

    override fun onPause() {
        super.onPause()

        val inputTextButton = findViewById<EditText>(R.id.edtText)
        val outputTextView = findViewById<TextView>(R.id.txtVwOutput)

        TranslationObject.currentFromText = inputTextButton.text.toString()
        TranslationObject.currentToText = outputTextView.text.toString()
    }

    override fun onResume() {
        super.onResume()

        val inputTextButton = findViewById<EditText>(R.id.edtText)
        val outputTextView = findViewById<TextView>(R.id.txtVwOutput)

        // update fields if needed (sent by caller)
        if (TranslationObject.currentFromText.isNotBlank()) {
            inputTextButton.setText(
                TranslationObject.currentFromText,
                TextView.BufferType.EDITABLE
            )
        }
        if (TranslationObject.currentToText.isNotBlank()) {
            outputTextView.text = TranslationObject.currentToText
        }
    }

    private fun initObserver() {
        viewModel.translatedText.observe(this) { value ->
            findViewById<TextView>(R.id.txtVwOutput).text = value
        }
    }
}

