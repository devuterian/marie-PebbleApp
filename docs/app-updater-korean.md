# Android 앱 업데이트와 한국어 UI

앱 버전: 1.12.0.1-marie-ver016

앱 시작 시와 기존 백그라운드 동기화 때 PebbleOAO GitHub 릴리즈를 확인합니다. 실행 중 자동 확인은 채널별로 6시간 간격을 두며 설정의 앱 업데이트는 즉시 확인합니다. 프리릴리즈 수신에 동의한 시계가 있으면 앱도 프리릴리즈를 포함합니다.

업로드할 APK 이름은 `Pebble-1.12.0.1-marie-ver016.apk` 형식을 유지하고 앱의 `marie-ver` 번호와 Android versionCode를 함께 올립니다. 펌웨어 태그 번호와 독립적으로 가장 높은 앱 번호를 선택합니다. GitHub가 제공하는 SHA-256, 패키지 이름, 설치된 앱과의 서명 일치, 높은 versionCode를 확인한 뒤 Android 설치 화면을 엽니다. 사용자가 설치를 확인합니다. 최초 1회는 이 업데이터가 포함된 APK를 설치해야 합니다.

한국어 UI는 시스템 언어를 따릅니다. 일반 UI는 `util/src/commonMain/kotlin/localization/Korean.kt`의 번역을 쓰며, 값이 들어가는 문장은 `localized(english, korean)`로 두 언어를 명시합니다. 한국어에서는 시스템 글꼴을 사용합니다. 통신 코드, 저장 키, 사용자 입력, 앱·기기 이름은 번역하지 않습니다. 외부 앱스토어·로그인 페이지·배터리 웹 서비스의 원문 콘텐츠는 해당 서비스가 제공하는 언어를 유지합니다.

검증: `:util:testAndroidHostTest`, `:androidApp:assembleRelease`. 실제 신규 공개 APK를 내려받아 Android 설치 확인까지 이어지는 경로는 후속 버전으로 별도 실기기 검증이 필요합니다.
