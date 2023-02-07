package com.nurshuvo.translateme.ui

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.data.repository.TranslationRepository
import com.nurshuvo.translateme.database.entity.TranslationFavorites
import com.nurshuvo.translateme.database.entity.TranslationHistory
import com.nurshuvo.translateme.ui.adapter.ListItemModel
import com.nurshuvo.translateme.ui.adapter.TranslationListAdapter
import com.nurshuvo.translateme.ui.adapter.countOfSelectionLiveData
import com.nurshuvo.translateme.ui.adapter.onClickedHistoryItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


// can be reused for history or favourite page
@AndroidEntryPoint
class TranslationListActivity : AppCompatActivity() {

    // TODO Will move this data to VM.
    private lateinit var listItemOrFavoriteModelList: MutableList<ListItemModel>
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

        // customize search view
        val searchItem = menu?.findItem(R.id.actionSearch)
        val searchView = searchItem?.actionView as SearchView
        customizeSearchView(searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty()) filter(newText)
                else {
                    lifecycleScope.launch {
                        updateAdapterWithDB()
                    }
                }
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    private fun customizeSearchView(searchView: SearchView) {
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).apply {
            setTextColor(resources.getColor(R.color.white))
            setHintTextColor(resources.getColor(R.color.white))
        }
        val searchClose =
            searchView.findViewById<View>(androidx.appcompat.R.id.search_close_btn) as ImageView
        ImageViewCompat.setImageTintList(searchClose, ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)))
    }
    private fun filter(text: String) {
        val filteredlist : MutableList<ListItemModel> = mutableListOf()

        // running a for loop to compare elements.
        for (item in listItemOrFavoriteModelList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.fromText.toLowerCase().contains(text.lowercase(Locale.getDefault()))
                || item.translatedText.toLowerCase().contains(text.lowercase(Locale.getDefault()))) {
                item.isSelected = false
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            val adapter = TranslationListAdapter(filteredlist, 0)
            (findViewById<View>(R.id.history_recycler_view) as RecyclerView).adapter =
                adapter
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.del -> {
                countOfSelectionLiveData.value = 0 // to update action bar title
                lifecycleScope.launch {
                    var isAtleastOneSelected = false
                    listItemOrFavoriteModelList.forEach {
                        // delete that row from table if selected true
                        if (it.isSelected) { // Delete specific entry
                            isAtleastOneSelected = true
                            if (isFavoriteListRequested) {
                                translationRepository.deleteFavoriteItem(
                                    TranslationFavorites(
                                        it.id,
                                        it.fromText,
                                        it.translatedText
                                    )
                                )
                            } else {
                                translationRepository.deleteHistoryItem(
                                    TranslationHistory(
                                        it.id,
                                        it.fromText,
                                        it.translatedText
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
                                        translationRepository.deleteAllFavorites()
                                    } else {
                                        translationRepository.deleteAllHistory()
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
        val allData = if (isFavoriteListRequested) {
            translationRepository.getAllTranslationFavorites()
        } else {
            translationRepository.getAllTranslationHistory()
        }
        listItemOrFavoriteModelList = mutableListOf()
        allData.forEach {
                when (it) {
                    is TranslationHistory -> {
                        listItemOrFavoriteModelList.add(
                            ListItemModel(
                                it.id,
                                it.fromText,
                                it.translatedText,
                                false
                            )
                        )
                    }
                    is TranslationFavorites -> {
                        listItemOrFavoriteModelList.add(
                            ListItemModel(
                                it.id,
                                it.fromText,
                                it.translatedText,
                                false
                            )
                        )
                    }
                }
        }
        val adapter = TranslationListAdapter(listItemOrFavoriteModelList, 0)
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