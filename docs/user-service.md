# user-service 기능 정의

## 1. 마이그레이션

### 1.1 users 테이블
사용자 계정 및 기본 정보를 저장한다.

포함 정보:
- 사용자 ID
- 이메일
- 비밀번호
- 이름
- 전화번호
- 계정 상태
- 생성일
- 수정일
- 탈퇴일

계정 상태:
- ACTIVE
- LOCKED
- WITHDRAWN

### 1.2 user_disabilities 테이블
사용자의 장애 특성 정보를 저장한다.

포함 정보:
- 사용자 ID
- 장애 유형
- 장애 정도
- 보조 필요사항
- 특이사항

---

## 2. 인증 API

### 2.1 회원가입
`POST /api/auth/signup`

기능:
- 이메일 중복 확인
- 비밀번호 암호화
- 사용자 생성
- 기본 계정 상태는 ACTIVE
- 장애 정보가 포함된 경우 user_disabilities에 함께 저장

---

### 2.2 로그인
`POST /api/auth/login`

기능:
- 이메일/비밀번호 검증
- 계정 상태 확인
- Access Token 발급
- Refresh Token 발급
- Refresh Token Redis 저장

정책:
- ACTIVE 계정만 로그인 가능
- LOCKED, WITHDRAWN 계정은 로그인 불가

---

### 2.3 로그아웃
`POST /api/auth/logout`

기능:
- 현재 사용자의 Refresh Token을 Redis에서 제거
- 필요 시 Access Token 블랙리스트 정책 고려

---

### 2.4 토큰 재발급
`POST /api/auth/reissue`

기능:
- Refresh Token 유효성 검증
- Redis에 저장된 Refresh Token과 비교
- 새로운 Access Token 발급
- 필요 시 Refresh Token rotation 적용

---

## 3. 사용자 API

### 3.1 내 사용자 정보 조회
`GET /api/users/me`

기능:
- JWT 인증 사용자 기준으로 내 정보 조회
- users와 user_disabilities 정보를 함께 반환
- 비밀번호는 절대 반환하지 않음

---

### 3.2 사용자 정보 수정
`PATCH /api/users/me`

기능:
- JWT 인증 사용자 기준으로 내 정보 수정
- 기본 정보와 장애 정보를 함께 수정 가능
- 트랜잭션 처리 필수

수정 가능 정보:
- 이름
- 전화번호
- 장애 유형
- 장애 정도
- 보조 필요사항
- 특이사항

수정 불가 정보:
- 사용자 ID
- 이메일
- 비밀번호
- 계정 상태

---

## 4. 내부 통신 API

### 4.1 내부 사용자 조회
`gRPC User Lookup`

기능:
- 다른 서비스에서 사용자 정보를 조회할 때 사용
- 서비스 간 통신은 gRPC를 기본으로 사용
- 내부 호출 전용 인증/인가 정책 적용 필수

반환 정보:
- userId
- loginId
- email
- name
- accountStatus
- disabilities
- desiredJob

주의:
- 비밀번호는 반환하지 않음
- 민감 정보는 최소한만 반환
- 기존 `/internal/users/{userId}` REST 엔드포인트는 임시 호환 또는 로컬 검증용으로만 유지 가능

---

## 5. 보안 정책

### 5.1 PasswordEncoder
- 비밀번호는 PasswordEncoder로 단방향 암호화한다.
- 평문 비밀번호 저장은 금지한다.

### 5.2 JWT
- Access Token은 API 인증에 사용한다.
- Refresh Token은 Redis에 저장한다.
- JWT Secret은 환경변수 또는 설정 파일을 통해 주입한다.
- 코드에 직접 하드코딩하지 않는다.

### 5.3 내부 API 보안
- gRPC 내부 통신은 별도 서비스 인증 정책을 적용한다.
- 임시 REST 내부 API가 존재하더라도 외부 사용자가 접근할 수 없도록 보호한다.
- 단순히 permitAll 처리하지 않는다.

---

## 6. 계정 상태 정책

### ACTIVE
- 정상 사용 가능
- 로그인 가능
- API 사용 가능

### LOCKED
- 로그인 불가
- 토큰 재발급 불가
- 관리자 또는 정책에 의해 잠긴 상태

### WITHDRAWN
- 로그인 불가
- 토큰 재발급 불가
- 탈퇴 처리된 상태
- 필요 시 soft delete 정책 적용

---

## 7. 테스트

작성해야 할 테스트:
- 회원가입 단위 테스트
- 로그인 단위 테스트
- 토큰 재발급 테스트
- 로그아웃 테스트
- 내 정보 조회 테스트
- 사용자 정보 수정 테스트
- 내부 사용자 조회 테스트
- 계정 상태별 로그인 제한 테스트
- Refresh Token Redis 저장/삭제 테스트
- 통합 테스트

테스트 시 검증할 것:
- 비밀번호 암호화 여부
- WITHDRAWN 계정 로그인 불가
- LOCKED 계정 로그인 불가
- Refresh Token 불일치 시 재발급 실패
- 사용자 정보 수정 시 users와 user_disabilities가 함께 반영되는지
