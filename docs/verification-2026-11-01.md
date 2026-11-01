# 서울 서버·앱 검증 · 2026-11-01

판정: 서버 조회와 게스트 접근 제한, 현재 빈 데이터 상태의 앱 동작은 통과. 실제 회원 가입/글 공유 서비스의 전체 검증은 미완료.

## 실제 서울 서버
- 프로젝트: mtlfnfhzvhkpwjkndcfl, ap-northeast-2, ACTIVE_HEALTHY.
- 게시글(작성자 관계 포함), 댓글, 승인 업체, 광고 API: HTTP 200.
- 게스트 게시글 등록: HTTP 401 / PostgreSQL 42501로 거부됨. 테스트 게시글 생성되지 않음.
- 게스트 공감자 정보 조회: HTTP 401로 거부됨.
- 존재하지 않는 게시글 공감 집계: 0 반환.
- 테이블 6개 전부 RLS 활성화, 정책 16개.
- 게스트 INSERT 및 일반 회원 hidden 수정 권한 없음.
- 카카오 제공자 비활성, 실제 회원 수 0명.
- 재현: `python3 backend/public-smoke.py` (게스트 쓰기 차단 확인 요청 포함).
- 상세 응답: `output/public-api-verification.json`. 키나 토큰은 출력하지 않음.

## 로컬 PostgreSQL 권한 시나리오
`npm test --prefix backend` 통과. PGlite PostgreSQL에서 실제 migration을 실행하되 auth.uid()는 테스트용으로 구성했다. 두 사용자의 공유 조회/댓글, 타인 글·댓글·프로필 수정 및 삭제 차단, 공감 중복 차단, 숨김 글·댓글 비노출, 미승인 업체·만료 광고 비노출을 확인했다. 실제 Supabase 로그인 계정으로 수행한 검증은 아니다.

## Android 에뮬레이터
ServerConnectionTest + BackendSafetyTest + CloudScreenTest: 5개 통과.
- Android에서 실제 서울 API 조회.
- Keystore 암호화 저장/삭제 및 잘못된 인증 콜백 차단.
- 현재 빈 서버에서 커뮤니티와 케어 로딩, 화면 재생성 후 복귀, 게스트 MY 표시.
- 기존 사용자 데이터를 초기화하거나 게시글을 생성하지 않음.
- 테스트 APK 빌드와 lint 성공. 결과: `output/current-verification-android.txt`.

## 남은 경고·미검증
1. SECURITY DEFINER 공감 합계 함수에 대한 게스트/회원 호출 경고 2개가 남아 있다. 고정 search_path와 공개 글만 집계하는 SQL을 사용해 숫자만 반환하며 의도된 공개 집계이지만, 경고가 해소된 상태라고 보고하지 않는다.
   - https://supabase.com/docs/guides/database/database-linter?lint=0028_anon_security_definer_function_executable
   - https://supabase.com/docs/guides/database/database-linter?lint=0029_authenticated_security_definer_function_executable
2. 카카오 실제 가입·로그인·토큰 갱신·로그아웃, 두 회원의 실제 REST 쓰기와 두 휴대폰 간 공유는 인증 설정과 테스트 계정이 없어 미검증.
3. 실물 휴대폰, 부하 테스트, 통신 끊김/복구는 미검증.
4. 공개 배포 전 약관/동의·탈퇴·신고/차단·남용 방지·백업/운영 체계 추가 필요.

다음 완료 기준: 카카오 설정 후 두 테스트 계정으로 실제 등록/조회/댓글/타인 수정 차단을 검증하고, 그 다음 실물 휴대폰에서 확인한다.

## 후속 실제 회원 검증 완료
2026-11-01 두 검증 회원의 실제 Auth 로그인·REST 공유/접근 제한·공감 테스트를 통과했다. 이전의 “테스트 계정 없어 실제 REST 미검증” 상태는 해소되었다. 카카오 OAuth 및 두 실물 휴대폰 검증은 아직 남아 있다. [회원 간 검증 결과](member-verification.md) 참고.
