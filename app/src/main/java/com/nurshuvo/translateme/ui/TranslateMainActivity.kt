package com.nurshuvo.translateme.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.navigation.NavigationView
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.database.entity.TranslationFavorites
import com.nurshuvo.translateme.database.entity.TranslationHistory
import com.nurshuvo.translateme.ui.utils.closeIme
import com.nurshuvo.translateme.ui.viewmodel.TranslateMainViewModel
import com.nurshuvo.translateme.util.TranslationObject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifImageView
import java.util.*

private const val TAG = "TranslateMainActivity"
private const val REQUEST_CODE_SPEECH_INPUT = 1

@AndroidEntryPoint
class TranslateMainActivity : AppCompatActivity(), DrawerLayout.DrawerListener {

    private val viewModel: TranslateMainViewModel by viewModels()
    private var drawerLayout: DrawerLayout? = null
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null
    private var navigationView: NavigationView? = null
    private var inputTextEditText: EditText? = null
    private var translateButton: ImageView? = null
    private var contentPasteOrCloseButton: ImageView? = null
    private var outputTextView: TextView? = null
    private var copyButton: ImageView? = null
    private var favButton: ImageView? = null
    private var frameLayout: FrameLayout? = null
    private var firstLanguage: TextView? = null
    private var toggleLanguage: ImageView? = null
    private var toggleParentLanguage: LinearLayout? = null
    private var secondLanguage: TextView? = null
    private var ivMic: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        drawerLayout = findViewById(R.id.my_drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        inputTextEditText = findViewById(R.id.edtText)
        translateButton = findViewById(R.id.btn)
        contentPasteOrCloseButton = findViewById(R.id.contentPaste)
        outputTextView = findViewById(R.id.txtVwOutput)
        copyButton = findViewById(R.id.copy_icon)
        favButton = findViewById(R.id.fav_icon)
        frameLayout = findViewById(R.id.frame_layout)
        firstLanguage = findViewById(R.id.firstLanguage)
        toggleLanguage = findViewById(R.id.ToggleLanguage)
        toggleParentLanguage = findViewById(R.id.ToggleParentLanguage)
        secondLanguage = findViewById(R.id.secondLanguage)
        ivMic = findViewById(R.id.iv_mic);

        setUpNavDrawer()
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu_icon_main)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setOnclickItemsOnNavigationDrawer()
        setOnClickListenerForTranslateButton()
        setOnClickListenerForCopyButton()
        setOnCLickOnFavButton()
        setOnCLickOnToggleButton()
        setOnCLickOnContentPasteOrCloseButton()
        setOnclickOnMicView()
        setListenerOnInputOutPutView()
        initObserver()
    }

    private fun setOnclickOnMicView() {
        ivMic?.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                if (viewModel.isBengaliToEnglish) "bn-BD"
                else Locale.getDefault()
            )
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
            try {
                startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
            } catch (e: Exception) {
                Toast.makeText(this, "  " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK) {
                val result = data?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                if (result != null && result.count() > 0) {
                    inputTextEditText?.postDelayed(
                        {
                            inputTextEditText?.setText(result[0])
                            // consider translate button is clicked
                            handleOnclickTranslateButton()
                        }, 100
                    )
                }
            }
        }
    }

    private fun setOnCLickOnToggleButton() {
        toggleLanguage?.setOnClickListener() {
            if (firstLanguage?.text == "Bengali") {
                firstLanguage?.text = "English"
                secondLanguage?.text = "Bengali"
                viewModel.isBengaliToEnglish = false
            } else {
                firstLanguage?.text = "Bengali"
                secondLanguage?.text = "English"
                viewModel.isBengaliToEnglish = true
            }
        }
        toggleParentLanguage?.setOnClickListener() {
            if (firstLanguage?.text == "Bengali") {
                firstLanguage?.text = "English"
                secondLanguage?.text = "Bengali"
                viewModel.isBengaliToEnglish = false
            } else {
                firstLanguage?.text = "Bengali"
                secondLanguage?.text = "English"
                viewModel.isBengaliToEnglish = true
            }
        }
    }

    private fun setOnCLickOnContentPasteOrCloseButton() {
        contentPasteOrCloseButton?.setOnClickListener() {
            if (inputTextEditText?.text?.isNotEmpty() == true) {
                inputTextEditText?.setText("")
            } else {
                // paste from clipboard copy
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                inputTextEditText?.setText(clipboard.text)
            }
        }
    }

    private fun setListenerOnInputOutPutView() {
        inputTextEditText?.addTextChangedListener() {
            if (it?.isNotEmpty() == true) {
                contentPasteOrCloseButton?.setImageResource(R.drawable.ic_baseline_close)
            } else {
                contentPasteOrCloseButton?.setImageResource(R.drawable.ic_outline_content_paste)
            }
        }
        outputTextView?.addTextChangedListener {
            if (it?.isNotEmpty() == true) {
                frameLayout?.visibility = View.VISIBLE
            }
        }
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
                viewModel.addToTranslationFavorites(
                    TranslationFavorites(
                        0,
                        fromText = inputTextEditText?.text.toString(),
                        translatedText = outputTextView?.text.toString()
                    )
                )
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
            handleOnclickTranslateButton()
        }
    }

    private fun handleOnclickTranslateButton() {
        Log.i(TAG, "Translation button clicked!")
        closeIme()
        val fromText = inputTextEditText?.text.toString()

        if (fromText.isBlank()) {
            return
        }

        // API call later when app is ready, as there is a API limit for now.
        // viewModel.translateToEnglish(fromText)

        // Translate by local ML model
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(if (viewModel.isBengaliToEnglish) TranslateLanguage.BENGALI else TranslateLanguage.ENGLISH)
            .setTargetLanguage(if (viewModel.isBengaliToEnglish) TranslateLanguage.ENGLISH else TranslateLanguage.BENGALI)
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

    private fun setUpNavDrawer() {
        // set header layout logo
        val v = (navigationView?.getHeaderView(0) as ViewGroup).getChildAt(0) as GifImageView
        Glide.with(this)
            .load(R.drawable.app_logo)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .circleCrop()
            .into(v)

        actionBarDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout?.addDrawerListener(actionBarDrawerToggle!!)
        drawerLayout?.setDrawerListener(this)
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

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        // Nothing
    }

    override fun onDrawerOpened(drawerView: View) {
        if (inputTextEditText?.isCursorVisible == true) {
            inputTextEditText?.isCursorVisible = false
        }
    }

    override fun onDrawerClosed(drawerView: View) {
        if (inputTextEditText?.isCursorVisible == false){
            inputTextEditText?.isCursorVisible = true
        }
    }

    override fun onDrawerStateChanged(newState: Int) {
        // Nothing
    }
}

