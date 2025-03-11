package com.tongueeye.wordcard

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update

@Dao
interface SettingDao {
    @Query("SELECT ttsSpeed FROM setting WHERE sid = 0")
    fun getTtsSpeed(): Float

    @Query("UPDATE setting SET ttsSpeed = :ttsSpeed WHERE sid = 0")
    fun setTtsSpeed(ttsSpeed: Float)

}