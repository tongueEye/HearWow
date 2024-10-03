package com.tongueeye.wordcard

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.tongueeye.wordcard.databinding.ActivityPlayQuizBinding
import com.tongueeye.wordcard.databinding.DialogConfirm2Binding
import com.tongueeye.wordcard.databinding.DialogConfirmBinding

class PlayQuizActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPlayQuizBinding

    private var unSolvedQuizzes: List<Quiz>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val db = AppDatabase.getDatabase(applicationContext)
        val quizDao = db?.quizDao()

        //맞추지 않은 문제들을 랜덤하게 섞어서 가져옴
        unSolvedQuizzes = quizDao?.getAllUnsolvedQuizzes()?.shuffled()
        //맞추지 않은 문제들을 순서대로 가져옴
        //unSolvedQuizzes = quizDao?.getAllUnsolvedQuizzes()

        // 풀리지 않은 퀴즈 목록이 비어있는지 확인
        if (unSolvedQuizzes!!.isNotEmpty()) {
            // 풀리지 않은 퀴즈 목록이 비어있지 않으면 퀴즈 데이터 로드 및 화면 설정
            loadQuizData(0) // 첫 번째 퀴즈 로드
        } else {
            // 풀리지 않은 퀴즈 목록이 비어있으면 알림 표시
            Toast.makeText(this, "퀴즈를 모두 맞췄네요! \uD83D\uDC4F \uD83D\uDC4F", Toast.LENGTH_SHORT).show()
            Toast.makeText(this, "퀴즈를 다시 맞추려면 모래시계 버튼을 클릭해주세요.", Toast.LENGTH_LONG).show()
            finish() // 액티비티 종료
        }

    }


    private fun loadQuizData(currentIdx: Int) {
        //quiz Data를 가져옴
        val db = AppDatabase.getDatabase(applicationContext)
        val quizDao = db?.quizDao()
        val quizData = unSolvedQuizzes
        var isImageZoomed = false // 이미지 확대 여부를 저장하기 위한 변수

        // 퀴즈 데이터가 null이거나 currentIdx가 퀴즈 데이터의 범위를 벗어나면 함수 종료
        if (quizData == null || currentIdx < 0 || currentIdx >= quizData.size) {
            return
        }

        val currentQuiz = quizData[currentIdx]

        binding.questionTV.text = currentQuiz.sentence
        binding.dialogTV.text = if (currentQuiz.isCorrect) "맞춘\n문제입니다!" else "맞춰봐!"

        // 배경색 변경
        if (currentQuiz.isCorrect) {
            binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.light_sky))
            binding.leftBtn.setImageResource(R.drawable.arrow_left_white)
            binding.rightBtn.setImageResource(R.drawable.arrow_right_white)
            binding.quizPaperIV.setImageResource(R.drawable.quiz_card_paper_white)
//            binding.playQuizBottom.setImageResource(R.drawable.play_quiz_bottom_grass)
            binding.exitBtn.setImageResource(R.drawable.exit_btn_white)
        } else{
            binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_night))
            binding.leftBtn.setImageResource(R.drawable.arrow_left)
            binding.rightBtn.setImageResource(R.drawable.arrow_right)
            binding.quizPaperIV.setImageResource(R.drawable.quiz_card_paper)
//            binding.playQuizBottom.setImageResource(R.drawable.gate_bottom)
            binding.exitBtn.setImageResource(R.drawable.exit_btn)
        }

        // 이미지 변경
        val imageResource = if (currentQuiz.isCorrect) {
            R.drawable.angel_icon // isCorrect가 true일 때
        } else {
            R.drawable.devil_icon // isCorrect가 false일 때
        }
        binding.devilCheckBtn.setImageResource(imageResource)

        val imageUri = currentQuiz.id.let { quizDao?.getImageUri(it)?.toUri() }
        Log.d("PlayQuizActivity", "Image URI: $imageUri")

        if (imageUri != null && imageUri.toString().isNotBlank()) {
            binding.questionIV.visibility = View.VISIBLE // 이미지가 있을 때 보이도록 설정
            binding.questionIV.contentDescription = "낱말 카드 이미지"
            Glide.with(this)
                .load(imageUri)
                .into(binding.questionIV)
            binding.questionIV.scaleX = 1.0f
            binding.questionIV.scaleY = 1.0f

            binding.questionIV.setOnClickListener {
                if (isImageZoomed) {
                    // 이미지가 확대된 상태이면 원래 크기로 돌아감
                    binding.questionIV.scaleX = 1.0f
                    binding.questionIV.scaleY = 1.0f
                } else {
                    // 이미지가 원래 크기인 상태이면 확대
                    binding.questionIV.scaleX = 2.5f
                    binding.questionIV.scaleY = 2.5f
                }
                isImageZoomed = !isImageZoomed // 이미지의 확대/축소 상태를 토글
            }

        } else{
            binding.questionIV.visibility = View.GONE // 이미지가 없을 때 숨김 처리
        }

        // rightBtn을 클릭했을 때, 인덱스를 증가시키고 퀴즈 사이즈 보다 크면 시 첫 인덱스로 돌아가도록 처리
        binding.rightBtn.setOnClickListener {
            val nextIdx = (currentIdx + 1) % quizData.size
            isImageZoomed = false
            loadQuizData(nextIdx)
        }

        // leftBtn을 클릭했을 때, 인덱스를 감소시키고 0보다 작으면 그 마지막 인덱스로 가도록 처리
        binding.leftBtn.setOnClickListener {
            val prevIdx = if (currentIdx - 1 < 0) quizData.size - 1 else currentIdx - 1
            isImageZoomed = false
            loadQuizData(prevIdx)
        }

        binding.devilCheckBtn.setOnClickListener {

            // 현재 퀴즈의 isCorrect 값을 토글
            quizData[currentIdx].isCorrect = !quizData[currentIdx].isCorrect
            quizData[currentIdx].id.let {
                // 데이터베이스에 업데이트
                quizDao?.updateQuiz(quizData[currentIdx])

                loadQuizData(currentIdx)
            }
        }

        binding.exitBtn.setOnClickListener {
            // quizAdapter 초기화
            if(binding.dialogTV.text == "맞춘\n문제입니다!"){
                val dialogBinding = DialogConfirm2Binding.inflate(LayoutInflater.from(this))
                val dialogBuilder = AlertDialog.Builder(this)
                val alertDialog = dialogBuilder.create()

                alertDialog.setView(dialogBinding.root)
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogBinding.confirmTextView.text="퀴즈를 종료할까요?"

                dialogBinding.noButton.setOnClickListener {
                    alertDialog.dismiss()
                }
                dialogBinding.yesButton.setOnClickListener {
                    alertDialog.dismiss()
                    finish()
                }
                alertDialog.show()
            }else{
                val dialogBinding = DialogConfirmBinding.inflate(LayoutInflater.from(this))
                val dialogBuilder = AlertDialog.Builder(this)
                val alertDialog = dialogBuilder.create()

                alertDialog.setView(dialogBinding.root)
                alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialogBinding.confirmTextView.text="퀴즈를 종료할까요?"

                dialogBinding.noButton.setOnClickListener {
                    alertDialog.dismiss()
                }
                dialogBinding.yesButton.setOnClickListener {
                    alertDialog.dismiss()
                    finish()
                }
                alertDialog.show()
            }

        }

    }
}
