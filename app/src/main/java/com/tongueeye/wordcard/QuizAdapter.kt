package com.tongueeye.wordcard

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tongueeye.wordcard.databinding.DialogConfirm2Binding
import com.tongueeye.wordcard.databinding.DialogDetailQuizBinding
import com.tongueeye.wordcard.databinding.ItemQuizBinding

class QuizAdapter(private val quizDao: QuizDao, private val quizActivity: MainActivity) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {
    private var quizList: MutableList<Quiz> = mutableListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]

        if (quiz.sentence.length > 15) {
            holder.binding.QuestionTV.text = "${quiz.sentence.substring(0, 15)}..."
        } else {
            holder.binding.QuestionTV.text = quiz.sentence
        }

        holder.binding.optionBtn.setOnClickListener {
            showPopupMenu(holder.binding.optionBtn, quiz)
        }

        holder.binding.QuestionTV.setOnClickListener {
            showDialogDetailQuiz(quiz)
        }

        // isCorrect에 따라 이미지 설정
        if (quiz.isCorrect == false){
            holder.binding.IconIV.setImageResource(R.drawable.x_icon)
        } else{
            holder.binding.IconIV.setImageResource(R.drawable.o_icon)
        }

    }

    override fun getItemCount() = quizList.size

    fun setQuizList(quizList: List<Quiz>){
        this.quizList.clear()
        this.quizList.addAll(quizList)
        notifyDataSetChanged()
    }

    private fun showPopupMenu(view: View, quiz: Quiz){
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.menu_option)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.action_edit -> {
                    //수정 작업 수행
                    showEditDialog(view, quiz)
                    true
                }
                R.id.action_delete -> {
                    //삭제 작업 수행
                    showDeleteComfirmDialog(view.context, quiz)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showEditDialog(view: View, quiz: Quiz){
        quizActivity.showEditQuizDialog(quiz)
    }

    private fun showDialogDetailQuiz(quiz: Quiz) {
        val dialogBinding = DialogDetailQuizBinding.inflate(LayoutInflater.from(quizActivity))
        val dialogBuilder = AlertDialog.Builder(quizActivity)
        val alertDialog = dialogBuilder.create()
        var isImageZoomed = false // 이미지 확대 여부를 저장하기 위한 변수

        // Set values
        dialogBinding.questionTV.text = quiz.sentence

        if (quiz.imageUri.isNullOrEmpty()) {
            dialogBinding.addPhotoIV.visibility = View.GONE
        } else {
            dialogBinding.addPhotoIV.visibility = View.VISIBLE
            Toast.makeText(quizActivity, quiz.imageUri!!.toUri().toString(), Toast.LENGTH_SHORT).show()
            Glide.with(quizActivity)
                .load(quiz.imageUri!!.toUri())
                .into(dialogBinding.addPhotoIV)
            dialogBinding.addPhotoIV.setOnClickListener {
                if (isImageZoomed) {
                    // 이미지가 확대된 상태이면 원래 크기로 돌아감
                    dialogBinding.addPhotoIV.scaleX = 1.0f
                    dialogBinding.addPhotoIV.scaleY = 1.0f
                } else {
                    // 이미지가 원래 크기인 상태이면 확대
                    dialogBinding.addPhotoIV.scaleX = 2.0f
                    dialogBinding.addPhotoIV.scaleY = 2.0f
                }
                isImageZoomed = !isImageZoomed // 이미지의 확대/축소 상태를 토글
            }
        }

        // TTS 버튼 클릭 리스너 추가
//        dialogBinding.playTtsBtn.setOnClickListener {
//            // TTS로 questionTV의 텍스트 읽기
//            quizActivity.speakOut(quiz.sentence)
//        }

        alertDialog.setView(dialogBinding.root)
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.closeBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun showDeleteComfirmDialog(context: Context, quiz: Quiz){
        val dialogBinding = DialogConfirm2Binding.inflate(LayoutInflater.from(context))
        val dialogBuilder = AlertDialog.Builder(context)
        val alertDialog = dialogBuilder.create()

        dialogBinding.confirmTextView.text = "낱말 카드를 삭제하시겠습니까?"

        alertDialog.setView(dialogBinding.root)
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.noButton.setOnClickListener {
            alertDialog.dismiss()
        }

        dialogBinding.yesButton.setOnClickListener {
            deleteQuiz(quiz)
            alertDialog.dismiss()
            Toast.makeText(context, "낱말 카드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
        }
        alertDialog.show()
    }

    private fun deleteQuiz(quiz: Quiz){
        quizDao.deleteQuiz(quiz)
        quizList.remove(quiz)
        notifyDataSetChanged() // 삭제된 항목을 즉시 반영하여 화면 갱신
    }

    class QuizViewHolder(val binding: ItemQuizBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}