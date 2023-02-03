package com.nurshuvo.translateme.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.nurshuvo.translateme.MyApplication
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.data.repository.TranslationRepository
import com.nurshuvo.translateme.database.entity.TranslationHistory
import com.nurshuvo.translateme.ui.adapter.HistoryAdapter
import com.nurshuvo.translateme.ui.adapter.HistoryModel
import com.nurshuvo.translateme.ui.adapter.countOfSelectionLiveData
import com.nurshuvo.translateme.ui.adapter.onClickedHistoryItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

// can be reused for history or favourite page
@AndroidEntryPoint
class TranslationListActivity : AppCompatActivity() {

    // TODO Will move this data to VM.
    private lateinit var historyOrFavoriteModelList: MutableList<HistoryModel>
    private var isFavoriteListRequested: Boolean = false

    @Inject
    lateinit var translationRepository: TranslationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        // data from Intent
        isFavoriteListRequested =
            intent?.extras?.getBoolean("key_isFavoriteListRequested", false) ?: false

        setSupportActionBar(findViewById(R.id.my_toolbar_history))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.action_bar_back_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isFavoriteListRequested) "Favourites" else "History"

        initObserver()

        val recyclerView = findViewById<View>(R.id.history_recycler_view) as RecyclerView

        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        itemDecorator.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider)!!)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )

        // Update recycler view adapter after getting data from DB
        lifecycleScope.launch {
            updateAdapterWithDB()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val findMenuItems = menuInflater
        findMenuItems.inflate(R.menu.history_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.del -> {
                countOfSelectionLiveData.value = 0 // to update action bar title
                lifecycleScope.launch {
                    var isAtleastOneSelected = false
                    historyOrFavoriteModelList.forEach {
                        // delete that row from table if selected true
                        if (it.isSelected) { // Delete specific entry
                            isAtleastOneSelected = true
                            if (isFavoriteListRequested) {
                                translationRepository.undoItemFavourite(
                                    it.fromText
                                )
                            } else {
                                translationRepository.deleteHistoryItem(
                                    TranslationHistory(
                                        it.id,
                                        it.fromText,
                                        it.translatedText,
                                        it.isFavourite
                                    )
                                )
                            }
                        }
                    }

                    if (!isAtleastOneSelected) { // User wants to delete all
                        AlertDialog.Builder(this@TranslationListActivity)
                            .setMessage("Do you want to clear all data?")
                            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                                lifecycleScope.launch {
                                    if (isFavoriteListRequested) {
                                        translationRepository.unDoAllFavoriteRecords()
                                    } else {
                                        translationRepository.deleteAll()
                                    }

                                    // update recycler view adapter with updated model list
                                    updateAdapterWithDB()
                                    Toast.makeText(
                                        this@TranslationListActivity,
                                        "Deleted!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton("No") { _: DialogInterface, _: Int ->
                                // Do nothing
                            }
                            .create()
                            .show()
                    } else {
                        Toast.makeText(this@TranslationListActivity, "Deleted!", Toast.LENGTH_SHORT)
                            .show()
                        // update recycler view adapter with updated model list
                        updateAdapterWithDB()
                    }
                }

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun initObserver() {
        onClickedHistoryItem.observe(this) {
            if (it) {
                onClickedHistoryItem.value = false
                finish()
            }
        }
        countOfSelectionLiveData.observe(this) { cnt ->
            if (cnt == 0) {
                supportActionBar?.title = if (isFavoriteListRequested) "Favourites" else "History"
            } else {
                supportActionBar?.title = "$cnt Selected"
            }
        }
    }

    private suspend fun updateAdapterWithDB() {
        val allHistoryData =
            translationRepository.getAllTranslationHistory()
        historyOrFavoriteModelList = mutableListOf()
        allHistoryData.forEach {
            if (isFavoriteListRequested && it.isFavourite) {
                historyOrFavoriteModelList.add(
                    HistoryModel(
                        it.id,
                        it.fromText,
                        it.translatedText,
                        it.isFavourite,
                        false
                    )
                )
            } else if (!isFavoriteListRequested) {
                historyOrFavoriteModelList.add(
                    HistoryModel(
                        it.id,
                        it.fromText,
                        it.translatedText,
                        it.isFavourite,
                        false
                    )
                )
            }
        }
        val adapter = HistoryAdapter(historyOrFavoriteModelList, 0)
        (findViewById<View>(R.id.history_recycler_view) as RecyclerView).adapter =
            adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        if (countOfSelectionLiveData.value == 0) {
            finish()
        } else {
            countOfSelectionLiveData.value = 0
            // refresh view
            lifecycleScope.launch {
                updateAdapterWithDB()
            }
        }
        return true
    }

    override fun onBackPressed() {
        if (countOfSelectionLiveData.value == 0) {
            finish()
        } else {
            countOfSelectionLiveData.value = 0
            // refresh view
            lifecycleScope.launch {
                updateAdapterWithDB()
            }
        }
    }
}