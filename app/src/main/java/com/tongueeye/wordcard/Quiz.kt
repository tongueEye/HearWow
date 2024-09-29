package com.tongueeye.wordcard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz")
data class Quiz(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var sentence: String,
    var imageUri: String? = null,
    var isCorrect: Boolean = false
)