# 서울 서버 배포 상태 · 2026-11-01

- 조직: teslounge (`bhytyzrqjjikyovvqgfn`), 무료 플랜.
- 사용 프로젝트: teslounge-seoul (`mtlfnfhzvhkpwjkndcfl`), 서울 `ap-northeast-2`.
- API URL: https://mtlfnfhzvhkpwjkndcfl.supabase.co
- 기존 아일랜드 프로젝트 `jrhtndphgyhpurvdrvja`: 무료 활성 프로젝트 한도 확보를 위해 일시정지. 삭제하지 않음. 중지 전 회원·게시글·댓글·공감·업체·광고·파일 0건 확인.
- 서울에 `teslounge_community` migration 적용. 테이블 6개, 정책 16개. 모두 RLS 활성화.
- 공개 키는 Git 제외 파일 `backend.local.properties`에 설정. 비밀 서버 키는 APK에 없음.

## 검증

- 실제 REST에서 게시판·댓글·업체·광고 읽기 요청 확인. 게시글 작성자 관계가 공감 관계와 모호했던 문제를 FK 명시로 수정.
- 클라우드 카탈로그: 게스트 게시글 INSERT 권한 없음, 회원 hidden 필드 UPDATE 권한 없음.
- 로컬 PostgreSQL 엔진의 두 사용자 권한/공감/광고 테스트 통과.
- 실제 클라우드의 임시 데이터 변경 기반 RLS 테스트는 도구 확인 요청 만료로 실행하지 못함. 이를 통과로 간주하지 않음.
- 보안 advisor: 공감 합계 함수 SECURITY DEFINER 호출 경고 2종(게스트/회원). 공개 글의 숫자 합계만 반환하고 공감자 ID나 숨김 글을 노출하지 않는 의도된 집계 경로이며 고정 search_path 사용. 로그인/수정 권한을 이 함수로 우회하지 않음.
  https://supabase.com/docs/guides/database/database-linter?lint=0028_anon_security_definer_function_executable
  https://supabase.com/docs/guides/database/database-linter?lint=0029_authenticated_security_definer_function_executable

- Android→서울 REST 조회/세션 보안 테스트 4개 통과, 빌드·lint·APK 서명 검증 완료.

## 남은 연결

Kakao 제공자는 `/auth/v1/settings`에서 비활성 확인. Kakao 개발자 앱의 REST 키/secret과 callback 설정이 필요하다. Supabase 연결 도구는 현재 Auth 설정 수정 기능을 제공하지 않는다.

- Kakao Redirect URI: `https://mtlfnfhzvhkpwjkndcfl.supabase.co/auth/v1/callback`
- Supabase Auth Redirect URLs: `kr.teslounge.app://auth/callback`

실제 가입/로그인과 두 휴대폰의 글 공유는 아직 미검증이다. 카카오 미설정 상태에서는 앱이 서버 연결과 가입 준비 상태를 구분해 안내한다. 약관, 탈퇴/연결 해제, 신고/차단, 남용 방지 등 공개 운영 준비는 server-setup.md 참고.

## 추가 검증
2026-11-01 실제 API 게스트 쓰기 차단, 공감자 비공개, Android 화면 포함 테스트 5개 통과. 범위와 미검증 항목은 [검증 보고서](verification-2026-11-01.md) 참조.

## 후속 실제 회원 검증 완료
2026-11-01 두 검증 회원의 실제 Auth 로그인·REST 공유/접근 제한·공감 테스트를 통과했다. 이전의 “테스트 계정 없어 실제 REST 미검증” 상태는 해소되었다. 카카오 OAuth 및 두 실물 휴대폰 검증은 아직 남아 있다. [회원 간 검증 결과](member-verification.md) 참고.
