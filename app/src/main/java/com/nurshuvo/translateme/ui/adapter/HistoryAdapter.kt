package com.nurshuvo.translateme.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.database.entity.TranslationHistory


class HistoryAdapter(
    private val historyList: List<TranslationHistory>
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val listItem: View = layoutInflater.inflate(
            R.layout.history_item_row,
            parent,
            false
        )
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fromTextView.text = historyList[position].fromText
        holder.translatedTextView.text = historyList[position].translatedText
        holder.parentLayout.setOnClickListener {
            // Clicked on a row
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var parentLayout = itemView.rootView
    var fromTextView: TextView = itemView.findViewById(R.id.fromText) as TextView
    var translatedTextView: TextView = itemView.findViewById(R.id.translatedText) as TextView
}