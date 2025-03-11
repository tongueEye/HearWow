package com.tongueeye.wordcard

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setting")
data class Setting(
    @PrimaryKey(autoGenerate = true)
    val sid: Int = 0,
    var ttsSpeed: Float
)