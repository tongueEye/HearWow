package com.tongueeye.wordcard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Quiz::class, Setting::class], version = 2)
abstract class AppDatabase() : RoomDatabase() {

    abstract fun quizDao(): QuizDao
    abstract fun settingDao(): SettingDao

    companion object{
        private var INSTANCE: AppDatabase?= null

        fun getDatabase(context: Context): AppDatabase?{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database")
                    .addCallback(object : RoomDatabase.Callback() { // 데이터베이스 초기화 시 실행되는 콜백 추가
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.settingDao()?.initSpeed() // 기본값 삽입
                            }
                        }
                    })
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }
    }

}