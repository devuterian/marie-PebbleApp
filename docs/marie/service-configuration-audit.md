# Marie 서비스 설정 점검 — ver015

기준은 저장해둔 공식 Android 1.11.0.3 APK와 현재 앱 소스입니다. 추가로 설치 직전 폰에서 가져온 정식 1.12.0.1 APK에서도 복구한 서비스 설정 7개가 모두 일치하는 것을 확인했습니다. 앱 전체 동작이 동일하다는 뜻은 아닙니다. 공식 APK의 설정과 실제 사용 위치를 확인해 복구했으며, 로그인된 사용자의 서버 응답 성공까지 확인한 결과는 아닙니다.

- Google 로그인: 공식 공개 OAuth 웹 클라이언트 ID 복구(ver013).
- 배터리 통계와 버그 리포트: 공식 dashboard API 주소 복구(ver014).
- 서버 푸시 토큰 등록: 공식 `/api/push-notifications/token` 주소 복구(ver015).
- Memfault: 공식 APK가 `Memfault-Project-Key` 헤더에 쓰는 공개 프로젝트 키 복구(ver015). 펌웨어 업데이트 조회와 시계 진단 업로드가 이 값을 사용함. 기존 `Send watch analytics` 비활성화 설정은 유지됨.
- Mixpanel: 공식 APK의 공개 수집용 프로젝트 토큰 복구(ver015). 관리자 API 키가 아님. 저장된 `Send app analytics` 설정을 SDK 초기화 때부터 적용하도록 보완함.
- Wispr: 기존 기본값이 공식 APK와 일치. 같은 주소를 gradle.properties에 명시함.
- Kirinki: 기존 기본값이 공식 APK와 일치. 같은 주소를 gradle.properties에 명시함.
- Apple/GitHub 로그인: Firebase OAuth provider 경로가 원본과 같음. 로그인 기능 플래그 모두 활성화. APK 내 GenericIdpActivity 콜백 설정 존재.
- Firebase: 로컬 google-services.json의 프로젝트 ID, 앱 ID, API key가 공식 APK 리소스와 일치함. 서명 지문 서버 등록 상태는 확인 불가.
- GitHub clientId/clientSecret: 현재 Android 경로에서 사용하지 않는 옛 속성. 그대로 비움. 서버 비밀키를 앱에 추가하지 않음.
- Cactus Pro key: 생성된 설정 필드 외에 현재 소스의 사용처가 없어 비움. 다른 프로젝트의 유료 라이선스 키를 복사하지 않음.
- Nenya/Notion OAuth backend: experimental 모듈의 기존 공개 기본 주소가 공식 APK와 일치함.
- 테스트용 Notion token: 테스트 전용 자격 증명으로 비움.
- QA: 기존 진단 UI를 위한 빌드 플래그 유지. 서비스 누락과 별개.

## 재발 방지

`preReleaseBuild`가 `:util:validateMarieReleaseConfig`를 실행함. 배터리, 푸시, Google, Memfault, Mixpanel, 음성 서비스의 필수 설정이 비었거나 더미이면 패키징 전에 실패함. URL은 HTTPS와 호스트 존재도 확인함. 값 자체는 오류 출력에 노출하지 않음.

빈 `bugUrl`을 주입한 실패 검사와 정상 release 빌드로 확인함. APK 내부에도 해당 공개 설정이 남아 있는지 검사함. 실제 로그인·푸시 수신·음성 인식·배터리 데이터 누적은 각각 실사용 확인이 필요함.

Memfault 프로젝트 키 공개 범위: https://docs.memfault.com/docs/ci/authentication
