# 🐌 히얼 와우 : 인공 와우 수술 후 청능 언어 재활을 도울 수 있는 앱

![히얼와우 썸네일](https://github.com/user-attachments/assets/04e4b10f-24b1-4a7e-b98d-bc13b67952d6)

* 개인 프로젝트
* 프로젝트 기간: '24.09.23~'24.10.20
* 프로젝트 목표: 인공 와우 수술 후 청능 언어 재활을 도울 수 있는 앱 개발
* 저작물의 형식: 모바일 어플리케이션


### 기획의도
* 인공 와우 수술 후, 재활을 위해 단어나 문장을 듣고 따라 말하는 연습을 해야 하는데 이를 혼자서도 공부할 수 있는 앱을 만들고자 하였습니다.
* 단순하고 직관적인 UI를 통해 연령 제한 없이 누구나 쉽게 조작할 수 있도록 하였습니다.
* UI 반응형 처리를 통해 태블릿/폰에서도 호환이 가능하도록 하였습니다.


### 기술스택
  ![Kotlin](https://img.shields.io/badge/Kotlin-007396?style=for-the-badge&logo=kotlin&logoColor=white)
  ![Android Studio](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
  ![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)


### 브랜드 및 컨셉
- Here Now, Hear Wow: 지금 이 공간의 소리를 듣다 


### 주요기능
* 낱말 카드 CRUD 기능
* 퀴즈 카드 랜덤 재생 기능
* TTS 음성 재생 기능
* 맞춤 여부 설정 기능
* 이미지 확대보기 기능


### 스토리보드
* 스플래시 화면
<img src="https://github.com/user-attachments/assets/26f3198f-672e-484a-a6b5-da804a7c4069" width="300px">

(1) 2초 동안 뜨고 사라지는 스플래시 화면


* 메인화면 (낱말 카드 목록 화면)
<img src="https://github.com/user-attachments/assets/486a5887-b639-4501-b61d-86b44244af84" width="300px">

(1) 낱말카드 목록이 보이며, 스크롤로 목록들을 내려 볼 수 있음.
(2) 맞춤 여부 표시
(3) 낱말 카드 텍스트가 표시되며, 클릭 시 낱말 카드를 팝업창으로 볼 수 있음.
(4) 클릭 시, 수정 및 삭제 메뉴가 보이며 이를 통해 입력한 낱말 카드를 수정하고 삭제할 수 있음.
(5) 클릭 시, 맞춤 여부가 초기화 됨.
(6) 클릭 시, 낱말 카드 추가 창이 나옴.
(7) 클릭 시, 낱말 카드가 랜덤으로 재생됨.

* 낱말 카드 추가 창
<img src="https://github.com/user-attachments/assets/a1f8417f-bbac-426b-9135-b938c185bd7e" width="300px">
(1) 클릭 시, 갤러리로 이동하며 원하는 사진을 선택할 수 있음.
(2) 입력창으로, 30자 까지 입력이 가능
(3) 클릭 시, 카드 생성 창이 닫힘
(4) 클릭 시, 낱말 카드가 목록에 추가됨


* 낱말 카드 재생 화면
<img src="https://github.com/user-attachments/assets/80c5b0b9-8239-4a7b-b519-f283920847f5" width="300px">
(1) 토글로 맞춤 여부를 변경할 수 있음
(2) 낱말에 대한 이미지이며, 클릭 시 2배로 확대해서 볼 수 있음
(3) 낱말 텍스트이며, 퀴즈 카드가 보여지거나 낱말 카드 클릭 시 이 텍스트가 TTS로 재생되는 데, 재생되는 동안 텍스트 색이 변경되어 보임.
(4) 이전 버튼 클릭 시, 이전 순서의 퀴즈 카드가 나타남.
(5) 다음 버튼 클릭 시, 다음 순서의 퀴즈 카드가 나타남.
(6) 클릭 시, 낱말 카드 재생을 종료하고 낱말 카드 목록 창으로 이동


