package com.nurshuvo.translateme.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nurshuvo.translateme.MyApplication
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.ui.adapter.HistoryAdapter
import kotlinx.coroutines.launch


class TranslationHistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)

        lifecycleScope.launch {
            val allHistoryData = (application as MyApplication).translationRepository.getAllTranslationHistory()
            val recyclerView = findViewById<View>(R.id.history_recycler_view) as RecyclerView
            val adapter = HistoryAdapter(allHistoryData)
            recyclerView.adapter = adapter
        }
    }
}