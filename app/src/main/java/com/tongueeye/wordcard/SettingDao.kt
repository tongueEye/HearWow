package com.tongueeye.wordcard

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SettingDao {

    @Query("SELECT ttsSpeed FROM setting WHERE sid = 0")
    fun getTtsSpeed(): Float

    @Query("UPDATE setting SET ttsSpeed = :ttsSpeed WHERE sid = 0")
    fun setTtsSpeed(ttsSpeed: Float)

    @Query("INSERT OR IGNORE INTO setting(sid, ttsSpeed) VALUES(0, 1.0)")
    fun initSpeed() // 값이 없을 때만 기본값 삽입

}