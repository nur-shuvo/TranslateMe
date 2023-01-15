package com.nurshuvo.translateme.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.nurshuvo.translateme.MyApplication
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.ui.adapter.HistoryAdapter
import com.nurshuvo.translateme.ui.adapter.onClickedHistoryItem
import kotlinx.coroutines.launch

class TranslationHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        setSupportActionBar(findViewById(R.id.my_toolbar_history))
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
            val allHistoryData =
                (application as MyApplication).translationRepository.getAllTranslationHistory()
            val adapter = HistoryAdapter(allHistoryData)
            recyclerView.adapter = adapter
        }
    }

    private fun initObserver() {
        onClickedHistoryItem.observe(this) {
            if (it) {
                onClickedHistoryItem.value = false
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}