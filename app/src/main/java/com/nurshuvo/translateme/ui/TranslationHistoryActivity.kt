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
import com.nurshuvo.translateme.database.entity.TranslationHistory
import com.nurshuvo.translateme.ui.adapter.HistoryAdapter
import com.nurshuvo.translateme.ui.adapter.HistoryModel
import com.nurshuvo.translateme.ui.adapter.countOfSelectionLiveData
import com.nurshuvo.translateme.ui.adapter.onClickedHistoryItem
import kotlinx.coroutines.launch

class TranslationHistoryActivity : AppCompatActivity() {

    // TODO Will move this data to VM.
    private lateinit var historyModelList: MutableList<HistoryModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        setSupportActionBar(findViewById(R.id.my_toolbar_history))
        supportActionBar?.setHomeAsUpIndicator(R.drawable.action_bar_back_icon)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
                    historyModelList.forEach {
                        // delete that row from table if selected true
                        if (it.isSelected) { // Delete specific entry
                            isAtleastOneSelected = true
                            (application as MyApplication).translationRepository.deleteHistoryItem(
                                TranslationHistory(it.id, it.fromText, it.translatedText)
                            )
                        }
                    }

                    if (!isAtleastOneSelected) { // User wants to delete all
                            AlertDialog.Builder(this@TranslationHistoryActivity)
                                .setMessage("Do you want to clear all data?")
                                .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                                    lifecycleScope.launch {
                                        // delete all from DB
                                        (application as MyApplication).translationRepository.deleteAll()
                                        // update recycler view adapter with updated model list
                                        updateAdapterWithDB()
                                        Toast.makeText(this@TranslationHistoryActivity, "Deleted!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton("No") { _: DialogInterface, _: Int ->
                                    // Do nothing
                                }
                                .create()
                                .show()
                    } else {
                        Toast.makeText(this@TranslationHistoryActivity, "Deleted!", Toast.LENGTH_SHORT).show()
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
                supportActionBar?.title = "History"
            } else {
                supportActionBar?.title = "$cnt Selected"
            }
        }
    }

    private suspend fun updateAdapterWithDB() {
        val allHistoryData =
            (application as MyApplication).translationRepository.getAllTranslationHistory()
        historyModelList = mutableListOf()
        allHistoryData.forEach {
            historyModelList.add(
                HistoryModel(it.id, it.fromText, it.translatedText, false)
            )
        }
        val adapter = HistoryAdapter(historyModelList, 0)
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