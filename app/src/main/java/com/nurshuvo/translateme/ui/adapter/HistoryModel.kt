package com.nurshuvo.translateme.ui.adapter

// Model class for recycler view adapter
data class HistoryModel(
    val id: Int,
    val fromText: String = "",
    val translatedText: String = "",
    var isSelected: Boolean = false
)