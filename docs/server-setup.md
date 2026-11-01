# 테슬라운지 공유 서버 설정

현재 상태: 서울 Supabase 프로젝트에 데이터베이스 설치 및 Android 연결 설정 완료. 카카오 제공자 설정과 실제 OAuth 검증은 남아 있다. 자세한 최신 상태는 deployment-status.md를 참고한다. 아래 생성/설치 절차는 새 환경을 만들 때만 사용하며 현재 프로젝트에 초기 migration을 재실행하지 않는다.

## 1. 사용자 소유 프로젝트 생성

1. https://supabase.com/dashboard/new 에서 로그인하고 조직을 선택한다.
2. 프로젝트 이름 `teslounge`, 새 데이터베이스 비밀번호, 한국에 가까운 리전을 지정한다. 사용 가능한 요금제를 확인하고 유료 변경은 별도로 결정한다.
3. 생성 완료 후 Project URL과 `sb_publishable_`로 시작하는 공개 키를 확인한다. 데이터베이스 비밀번호, `sb_secret_`, `service_role`, Kakao client secret은 대화나 APK에 넣지 않는다.

## 2. 데이터베이스 설치

새 프로젝트의 SQL Editor에서 `supabase/migrations/202611010001_community.sql` 전체를 실행한다. 한 트랜잭션이며 신규 프로젝트에 한 번만 실행한다. 기존 운영 프로젝트에 실행할 경우 먼저 충돌 여부와 백업을 확인한다.

테이블:
- profiles: 공개 커뮤니티 닉네임과 사용자 유형. 이메일·카카오 실명 없음.
- posts/comments: 공개 읽기, 작성자만 수정/삭제, 숨김 처리는 관리자만 가능.
- reactions: 사용자별 중복 공감 금지, 다른 회원의 공감자 ID는 비공개. 집계 함수만 공개.
- providers: 승인된 케어 업체만 공개.
- advertisements: 승인 상태, 기간, 연결 업체 승인 상태가 모두 맞을 때 공개.

일반 회원은 업체·광고를 등록/승인할 수 없다. 초기 운영자는 Supabase Table Editor에서 내용을 검토한 후 approved를 설정한다. 실제 광고만 등록하고 제목에 광고임을 알 수 있게 한다. 예약·결제 API는 포함하지 않는다.

## 3. Kakao 설정

1. https://developers.kakao.com 에서 앱을 만든다.
2. Kakao Login을 활성화하고 REST API 키와 client secret을 Supabase Authentication → Providers → Kakao에 등록한다.
3. Kakao의 Redirect URI에는 Supabase에 표시되는 `https://PROJECT_REF.supabase.co/auth/v1/callback`을 등록한다.
4. Supabase Authentication → URL Configuration → Redirect URLs에는 `kr.teslounge.app://auth/callback`을 등록한다. 광범위한 와일드카드는 사용하지 않는다.
5. 이메일이 필요 없다면 카카오 이메일 동의 요청을 생략하고 Supabase Kakao 제공자의 Allow users without an email 설정을 켠다. 요청하는 동의항목은 실제 필요한 최소 범위로 설정한다.

앱은 시스템 브라우저 + PKCE를 사용한다. 카카오톡 앱을 직접 호출하는 네이티브 SDK 방식은 아니다. 로그인 성공 후 커뮤니티로 돌아오며 첫 참여 시 서비스 닉네임을 따로 받는다. 현재 로그인 전 작성하던 원격 글 초안 복구 기능은 없다.

## 4. Android 설정과 빌드

루트 `backend.local.properties.example`을 `backend.local.properties`로 복사하고 URL과 공개 키를 입력한다. 이 로컬 파일은 Git에서 제외되어 있다. 이 파일의 두 값은 APK에 포함되므로 공개 키만 허용한다. 서버 접근 보호는 비밀 공개 키가 아니라 RLS가 담당한다.

```
bash scripts/build-local.sh assembleDebug lintDebug
```

설정이 있으면 홈에서 로컬 예시 글 대신 공유 게시판으로 연결된다. 커뮤니티는 서버만 읽고 쓰며 기존 로컬 게시글을 이전하지 않는다. 기기별 화면 설정과 체크리스트는 계속 로컬이다. 케어는 서버의 승인된 업체와 광고를 조회한다. 현재 게시글·댓글은 최대 100개, 업체는 최대 200개를 읽으며 대규모 서비스의 검색/페이지 처리는 후속 작업이다.

세션은 Android Keystore AES-GCM으로 암호화 저장하고 만료 전에 갱신한다. 로그아웃 시 로컬 토큰을 제거하고 서버 세션 종료를 요청한다. 통신 장애로 서버 로그아웃이 실패해도 기기 로그아웃은 완료되며 안내한다.

## 5. 검증

로컬 SQL/RLS 검증:
```
npm ci --prefix backend --ignore-scripts
npm test --prefix backend
```

PGlite 실제 PostgreSQL 엔진에 Supabase의 `auth.users`/`auth.uid()` 최소 테스트 대역을 만든 뒤 migration 그대로 실행한다. DB 권한 검증이며 실제 Supabase Auth나 네트워크 배포 검증을 대신하지 않는다.

실제 클라우드 검증은 별도 테스트 프로젝트에서 확인된 이메일 테스트 계정 두 개를 만든 후 실행한다. 비밀번호는 셸 기록에 직접 적지 말고 로컬 비밀 환경변수로 제공한다.

필요 환경변수: `SUPABASE_URL`, `SUPABASE_PUBLISHABLE_KEY`, `TEST_A_EMAIL`, `TEST_A_PASSWORD`, `TEST_B_EMAIL`, `TEST_B_PASSWORD`.
```
python3 backend/smoke.py
```
이 스크립트는 테스트 글/댓글을 만들고 정리한다. 테스트 계정에 프로필이 없으면 검증용 프로필을 만들며 계정/프로필은 남긴다. 다른 사람의 데이터에는 쓰지 않는다. 성공해도 실제 Android 카카오 로그인과 세션 갱신은 두 기기에서 별도로 확인한다.

## 6. 운영 전 남은 일

현재 구성은 소규모 비공개 테스트용이다. 공개 서비스 전에 이용약관/개인정보 처리방침과 동의 기록, 회원 탈퇴·카카오 연결 해제, 신고·차단, 쓰기 속도 제한, 모니터링과 백업/복구, 운영 담당자 지정이 필요하다. 직접 클라이언트 쓰기를 허용하므로 RLS 외에 서비스별 남용 방지 정책도 추가한다. 운영용 링크/약관을 임의로 만들거나 동의한 것으로 처리하지 않는다.

공식 자료:
- https://supabase.com/docs/guides/auth/social-sign-in/auth-kakao
- https://supabase.com/docs/guides/auth/sessions/pkce-flow
- https://supabase.com/docs/guides/database/postgres/row-level-security
