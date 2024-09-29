package com.tongueeye.wordcard

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Quiz::class], version = 1)
abstract class AppDatabase() : RoomDatabase() {

    abstract fun quizDao(): QuizDao

    companion object{
        private var INSTANCE: AppDatabase?= null

        fun getDatabase(context: Context): AppDatabase?{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database")
                    .allowMainThreadQueries()
                    .build()
            }
            return INSTANCE
        }
    }

}