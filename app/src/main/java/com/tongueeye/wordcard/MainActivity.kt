package com.tongueeye.wordcard

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.tongueeye.wordcard.databinding.ActivityMainBinding
import com.tongueeye.wordcard.databinding.DialogConfirm2Binding
import com.tongueeye.wordcard.databinding.DialogConfirm3Binding
import com.tongueeye.wordcard.databinding.DialogCreateQuizBinding
import com.tongueeye.wordcard.databinding.DialogSettingSpeedBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var quizAdapter: QuizAdapter

    private var selectedImageUri: Uri?=null // 이미지를 저장할 변수 추가
    private lateinit var dialogBinding: DialogCreateQuizBinding // 다이얼로그 바인딩 변수 추가
    private lateinit var settingDialogBinding: DialogSettingSpeedBinding

    private val PERMISSION_REQUEST_CODE = 1001 //권한 요청 코드
    private var DELETE_IMAGE_CHECK = 0 //이미지 삭제 버튼 클릭 여부 변수

    private lateinit var tts: TextToSpeech //tts 기능 사용하기 위한 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TTS 초기화
        tts = TextToSpeech(this, this)

        //Appdatabase 초기화
        val db = AppDatabase.getDatabase(applicationContext)
        val quizDao = db?.quizDao()
        
        binding.speedBtn.setOnClickListener { 
            showSpeedSettingDialog()
        }

        binding.correctResetBtn.setOnClickListener {
            showResetCorrectDialog()
        }

        binding.addQuizBtn.setOnClickListener {
            showCreateQuizDialog()
        }

        binding.PlayQuizBtn.setOnClickListener {
            if (quizDao != null) {
                //showPlayQuizDialog(quizDao)

                val quizList = quizDao.getAllQuizzes()
                if (quizList.isNotEmpty()) {
                    val intent = Intent(this@MainActivity, PlayQuizActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, "공부할 카드가 없습니다. 카드를 추가해주세요!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //RecyclerView 설정
        binding.quizListRV.layoutManager = LinearLayoutManager(this)

        // quizAdapter 초기화
        quizAdapter = quizDao?.let { QuizAdapter(it, this) }!!

        //Adapter 적용
        binding.quizListRV.adapter = quizAdapter

        // 처음 QuizActivity에 들어갈 때 퀴즈 목록을 화면에 표시
        loadQuizList()
    }

    override fun onResume() {
        super.onResume()
        // 다시 화면으로 돌아올 때 데이터를 다시 로드하여 IconIV 바로 갱신
        loadQuizList()
    }

    private val PICK_IMAGE_REQUEST = 1
    
    
    fun showSpeedSettingDialog(){

        settingDialogBinding = DialogSettingSpeedBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(settingDialogBinding.root)
        val dialog = dialogBuilder.create()

        // 다이얼로그 배경을 투명색으로 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // DB에서 현재 저장된 속도 값 가져오기
        val settingDao = AppDatabase.getDatabase(applicationContext)?.settingDao()
        var speedValue = settingDao?.getTtsSpeed() ?: 1.0f  // 기본값 1.0f

        // UI에 현재 속도 반영
        settingDialogBinding.quizEditText.setText(String.format("%.1f", speedValue))

        // 감소 버튼 클릭 시
        settingDialogBinding.decreaseBtn.setOnClickListener {
            if (speedValue > 0.5f) { // 최소 속도 제한
                speedValue -= 0.1f
                settingDialogBinding.quizEditText.setText(String.format("%.1f", speedValue))
            }
        }

        // 증가 버튼 클릭 시
        settingDialogBinding.increaseBtn.setOnClickListener {
            if (speedValue < 2.0f) { // 최대 속도 제한
                speedValue += 0.1f
                settingDialogBinding.quizEditText.setText(String.format("%.1f", speedValue))
            }
        }

        // 다이얼로그 내 버튼 클릭 이벤트 처리
        settingDialogBinding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        settingDialogBinding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 처리할 작업 수행
            settingDao?.setTtsSpeed(speedValue)
            Toast.makeText(this, "속도 저장 완료! ($speedValue)", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()


    }

    fun showCreateQuizDialog() {
        dialogBinding = DialogCreateQuizBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialog = dialogBuilder.create()

        // 다이얼로그 배경을 투명색으로 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.addPhotoIV.setOnClickListener {
            checkAndRequestPermission()
        }

        dialogBinding.imageDeleteBtn.setOnClickListener {
            Toast.makeText(this,"이미지를 삭제합니다.", Toast.LENGTH_SHORT).show()
            // 이미지를 기본 이미지로 변경
            dialogBinding.addPhotoIV.setImageResource(R.drawable.add_photo_white)
            // 이미지 삭제 버튼 숨기기
            dialogBinding.imageDeleteBtn.visibility = View.GONE

            // 이미지 삭제 버튼이 클릭 되면 값을 1로 변경
            DELETE_IMAGE_CHECK = 1

        }

        dialogBinding.quizEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 변경 중
                dialogBinding.qtextCountTV.text="(${s?.length}자/30자)"
            }
            override fun afterTextChanged(s: Editable?) {
            }

        })


        // 다이얼로그 내 버튼 클릭 이벤트 처리
        dialogBinding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 처리할 작업 수행
            val quizText = dialogBinding.quizEditText.text.toString()

            if (quizText.isEmpty()) {
                Toast.makeText(this, "단어나 문장을 입력해 주세요!", Toast.LENGTH_SHORT).show()
            } else {
                if (DELETE_IMAGE_CHECK == 1){ // 사진이 선택되지 않고 이미지 삭제 버튼이 눌린경우
                    saveQuiz(quizText,null)
                } else{
                    saveQuiz(quizText, selectedImageUri)
                }
                selectedImageUri = null
                dialog.dismiss()
            }

        }
        dialog.show()
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // 이미 권한이 있는 경우 갤러리 열기
            openGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨
                openGallery()
            } else {
                // 권한 거부됨
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        DELETE_IMAGE_CHECK = 0 //이미지가 선택되면 0으로 변경
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri = data.data!!
            // 선택한 이미지를 addPhotoIV 이미지 뷰에 표시
            dialogBinding.addPhotoIV.setImageURI(imageUri)
            selectedImageUri = imageUri

            // 이미지가 선택된 후에 Glide를 사용하여 이미지를 설정합니다.
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.add_photo_white) // 기본 이미지 설정
                .into(dialogBinding.addPhotoIV)

            if (imageUri == null) {
                dialogBinding.imageDeleteBtn.visibility = View.GONE
            } else {
                dialogBinding.imageDeleteBtn.visibility = View.VISIBLE
            }
        }
    }


    private fun saveQuiz(quizText: String, imageUri: Uri?) {
        val imageUriString = imageUri?.toString() ?: ""
        val quiz = Quiz(sentence = quizText, imageUri = imageUriString)
        AppDatabase.getDatabase(applicationContext)?.quizDao()?.insertQuiz(quiz)
        Toast.makeText(this, "낱말 카드가 추가되었습니다.", Toast.LENGTH_SHORT).show()
        loadQuizList()
    }

    private fun loadQuizList() {
        val quizList: List<Quiz> = AppDatabase.getDatabase(applicationContext)?.quizDao()?.getAllQuizzes()
            ?: emptyList()

        if (quizList.isNotEmpty()){
            //데이터 적용
            quizAdapter.setQuizList(quizList)
        }
    }

    private fun updateQuiz(id: Int, sentence: String, imageUri: Uri?) {
        val imageUriString = imageUri?.toString() ?: ""
        val updatedQuiz = Quiz(id = id, sentence = sentence, imageUri = imageUriString)
        AppDatabase.getDatabase(applicationContext)?.quizDao()?.updateQuiz(updatedQuiz)
    }

    fun showEditQuizDialog(quiz: Quiz){
        dialogBinding = DialogCreateQuizBinding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
        val dialog = dialogBuilder.create()

        // 다이얼로그 배경을 투명색으로 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //Appdatabase 초기화
        val db = AppDatabase.getDatabase(applicationContext)
        val quizDao = db?.quizDao()

        // 이미지 URI 가져오기
        val imageUri = quizDao?.getImageUri(quiz.id)?.toUri()

        // 기존 퀴즈 정보 설정
        dialogBinding.quizEditText.setText(quiz.sentence)
        if (imageUri != null && imageUri.toString().isNotBlank()) {
            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.add_photo_white) // 기본 이미지 설정
                .into(dialogBinding.addPhotoIV)
            dialogBinding.imageDeleteBtn.visibility = View.VISIBLE
        } else {
            dialogBinding.addPhotoIV.setImageResource(R.drawable.add_photo_white)
            dialogBinding.imageDeleteBtn.visibility = View.GONE
        }

        dialogBinding.addPhotoIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }


        dialogBinding.imageDeleteBtn.setOnClickListener {
            Toast.makeText(this,"이미지를 삭제합니다.",Toast.LENGTH_SHORT).show()
            // 이미지를 기본 이미지로 변경
            dialogBinding.addPhotoIV.setImageResource(R.drawable.add_photo_white)
            // 이미지 삭제 버튼 숨기기
            dialogBinding.imageDeleteBtn.visibility = View.GONE

            // 이미지 삭제 버튼이 클릭 되면 값을 1로 변경
            DELETE_IMAGE_CHECK = 1
        }

        dialogBinding.qtextCountTV.text="(${dialogBinding.quizEditText.text.length}자/30자)"
        dialogBinding.quizEditText.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 변경 중
                dialogBinding.qtextCountTV.text="(${s?.length}자/30자)"
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        // 다이얼로그 내 버튼 클릭 이벤트 처리
        dialogBinding.cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.saveBtn.setOnClickListener {
            // 저장 버튼 클릭 시 처리할 작업 수행
            val quizText = dialogBinding.quizEditText.text.toString()

            if (quizText.isEmpty()) {
                Toast.makeText(this, "단어나 문장을 입력해주세요!", Toast.LENGTH_SHORT).show()
            } else {
                val currentImage = quizDao?.getImageUri(quiz.id)?.toUri()
                if (selectedImageUri != null) { //갤러리에서 새로 사진을 선택한 경우
                    // 선택한 사진을 저장
                    updateQuiz(quiz.id, quizText, selectedImageUri)
                } else { //갤러리에서 새로 사진을 선택하지 않은 경우
                    // 기존의 이미지를 그대로 저장
                    updateQuiz(quiz.id, quizText, currentImage)
                }
                if (DELETE_IMAGE_CHECK == 1) { // 사진이 선택되지 않고 이미지 삭제 버튼이 눌린경우
                    quizDao?.updateQuizImageUri(quiz.id) //DB에 저장된 이미지를 null로 바꿈
                }
                Toast.makeText(this, "낱말 카드가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                loadQuizList()
                selectedImageUri = null
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    //TTS 초기 설정
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 한국어로 설정
            val result = tts.setLanguage(Locale.KOREAN)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS 언어를 사용할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "TTS 초기화 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 액티비티가 종료될 때 TTS 리소스를 해제
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }


    fun showResetCorrectDialog(){
        val confirm2Binding = DialogConfirm2Binding.inflate(LayoutInflater.from(this))
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(confirm2Binding.root)
        val dialog = dialogBuilder.create()

        // 다이얼로그 배경을 투명색으로 설정
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        confirm2Binding.confirmTextView.text = "맞춘 카드를 초기화 할까요?"
        confirm2Binding.noButton.setOnClickListener {
            dialog.dismiss()
        }
        confirm2Binding.yesButton.setOnClickListener {
            // 모든 퀴즈의 isCorrect 값을 false로 업데이트
            val db = AppDatabase.getDatabase(applicationContext)
            val quizDao = db?.quizDao()
            val quizList = quizDao?.getAllQuizzes()

            if (quizList!!.isNotEmpty()){

                val quizzes = quizDao.getAllQuizzes()
                quizzes.forEach { quiz ->
                    quiz.isCorrect = false
                    quiz.id.let { quizId ->
                        quizDao.updateQuiz(quiz)
                    }
                }

                loadQuizList()
                Toast.makeText(this, "채점이 취소 되었습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

            } else{
                Toast.makeText(this, "다시 채점할 카드가 없습니다.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        }
        dialog.show()
    }

//    fun showPlayQuizDialog(quizDao: QuizDao){
//        val confirmDialogBinding = DialogConfirm3Binding.inflate(LayoutInflater.from(this))
//        val dialogBuilder = AlertDialog.Builder(this)
//            .setView(confirmDialogBinding.root)
//        val dialog = dialogBuilder.create()
//
//        // 다이얼로그 배경을 투명색으로 설정
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        // 퀴즈 목록 수 가져오기
//        val quizListCount = quizDao.getAllQuizzes().size
//        // isCurrent 속성값이 false인 것의 수 가져오기
//        val notCorrectCount = quizDao.getAllQuizzes().count { !it.isCorrect }
//        confirmDialogBinding.confirmTextView.text = "퀴즈를 시작할까요?"
//        confirmDialogBinding.quizCntTV.text = "(풀 문제: ${notCorrectCount} / 전체 문제: ${quizListCount})"
//
//        confirmDialogBinding.noButton.setOnClickListener {
//            dialog.dismiss()
//        }
//
//        confirmDialogBinding.yesButton.setOnClickListener {
//            val quizList = quizDao.getAllQuizzes()
//            if (quizList.isNotEmpty()) {
//                val intent = Intent(this@MainActivity, PlayQuizActivity::class.java)
//                startActivity(intent)
//            } else {
//                Toast.makeText(this@MainActivity, "퀴즈가 없습니다. 퀴즈를 추가해주세요!", Toast.LENGTH_SHORT).show()
//            }
//            dialog.dismiss()
//        }
//        dialog.show()
//    }

}