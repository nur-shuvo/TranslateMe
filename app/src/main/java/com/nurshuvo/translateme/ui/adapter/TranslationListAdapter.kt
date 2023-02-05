package com.nurshuvo.translateme.ui.adapter

import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.nurshuvo.translateme.R
import com.nurshuvo.translateme.util.TranslationObject

val onClickedHistoryItem: MutableLiveData<Boolean> = MutableLiveData()
val countOfSelectionLiveData: MutableLiveData<Int> = MutableLiveData()

// TODO: There are some duplicate code blocks, will clean it
class TranslationListAdapter(
    private val historhistoryOrFavoriteModelList: List<ListItemModel>,
    private var countOfSelection: Int
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
        holder.fromTextView.text = historhistoryOrFavoriteModelList[position].fromText
        holder.translatedTextView.text = historhistoryOrFavoriteModelList[position].translatedText

        if (historhistoryOrFavoriteModelList[position].isSelected) {
            holder.parentLayout.background = ColorDrawable(
                (holder.parentLayout.context.resources).getColor(
                    R.color.white_back
                )
            )
        } else {
            val outValue = TypedValue()
            holder.parentLayout.context.theme
                .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            holder.parentLayout.setBackgroundResource(outValue.resourceId)
        }

        holder.parentLayout.setOnClickListener { // onCLicked
            if (historhistoryOrFavoriteModelList[position].isSelected) {
                val outValue = TypedValue()
                holder.parentLayout.context.theme
                    .resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                holder.parentLayout.setBackgroundResource(outValue.resourceId)
                historhistoryOrFavoriteModelList[position].isSelected = false
                countOfSelection--
                countOfSelectionLiveData.value = countOfSelection
            } else {
                if (countOfSelection == 0) {
                    // Clicked on a row item for go back to the main page
                    // Go to the main page with the values (fromText and toText)
                    TranslationObject.currentFromText = historhistoryOrFavoriteModelList[position].fromText
                    TranslationObject.currentToText = historhistoryOrFavoriteModelList[position].translatedText
                    onClickedHistoryItem.value = true
                } else { // countOfSelection > 0
                    holder.parentLayout.background = ColorDrawable(
                        (holder.parentLayout.context.resources).getColor(
                            R.color.white_back
                        )
                    )
                    historhistoryOrFavoriteModelList[position].isSelected = true
                    countOfSelection++
                    countOfSelectionLiveData.value = countOfSelection
                }
            }
        }

        holder.parentLayout.setOnLongClickListener {
            if (!historhistoryOrFavoriteModelList[position].isSelected) {
                holder.parentLayout.background = ColorDrawable(
                    (holder.parentLayout.context.resources).getColor(
                        R.color.white_back
                    )
                )
                historhistoryOrFavoriteModelList[position].isSelected = true
                countOfSelection++
                countOfSelectionLiveData.value = countOfSelection
            }
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return historhistoryOrFavoriteModelList.size
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var parentLayout: View = itemView.rootView
    var fromTextView: TextView = itemView.findViewById(R.id.fromText) as TextView
    var translatedTextView: TextView = itemView.findViewById(R.id.translatedText) as TextView
}