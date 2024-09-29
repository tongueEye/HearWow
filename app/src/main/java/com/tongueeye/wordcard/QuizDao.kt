package com.tongueeye.wordcard

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz")
    fun getAllQuizzes(): List<Quiz>

    @Insert
    fun insertQuiz(quiz: Quiz)

    @Update
    fun updateQuiz(quiz: Quiz)

    @Delete
    fun deleteQuiz(quiz: Quiz)

    @Query("SELECT imageUri FROM quiz WHERE id = :quizId")
    fun getImageUri(quizId: Int): String?

    @Query("UPDATE quiz SET imageUri = NULL WHERE id = :quizId")
    fun updateQuizImageUri(quizId: Int)

    @Query("DELETE FROM quiz WHERE id = :quizId")
    fun DeleteQuizById(quizId: Int): Int

    @Query("SELECT * FROM quiz WHERE isCorrect = 0")
    fun getAllUnsolvedQuizzes(): List<Quiz>?
}