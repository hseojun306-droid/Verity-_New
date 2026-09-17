# Verity Android v2
실제 Verity v3 서버의 `/api/chat`에 연결하는 Android 앱 프로젝트입니다.

## 연결 방법
`MainActivity.java`의 SERVER_URL을 Render에 배포한 Verity 서버 주소로 바꾸세요.
예: https://내-베리티-서버.onrender.com

API 키는 Android 앱에 넣지 않습니다. API 키는 기존 Node/Express 서버의 환경변수에만 둡니다.

Android Studio에서 프로젝트를 열고 Gradle Sync → Run 하면 됩니다.
