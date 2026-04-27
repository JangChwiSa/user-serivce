# User Service API Spec

User Service는 `user_db`를 사용한다.

---

# 1. 공통 정책

## 1.1 인증 방식

- 인증 방식은 JWT를 사용한다.
- Access Token은 API 인증에 사용한다.
- Refresh Token은 Redis에 저장한다.

## 1.2 Refresh Token 정책 (Redis)

```text
key = refresh_token:{userId}
value = refreshToken
TTL = Refresh Token 만료 시간
````

정책:

* 로그인 시 기존 Refresh Token은 덮어쓴다.
* 로그아웃 시 Redis에서 삭제한다.
* 재발급 시 Redis 값과 비교한다.

---

## 1.3 계정 상태

| 상태        | 설명       |
| --------- | -------- |
| ACTIVE    | 정상 사용 가능 |
| LOCKED    | 로그인 불가   |
| WITHDRAWN | 탈퇴 계정    |

---

## 1.4 공통 Header

| Header        | 필수 | 설명                  |
| ------------- | -- | ------------------- |
| Authorization | Y  | Bearer Access Token |

---

## 1.5 공통 에러 응답

```json
{
  "code": "INVALID_TOKEN",
  "message": "유효하지 않은 토큰입니다."
}
```

대표 에러 코드:

| Code                         | 설명                |
| ---------------------------- | ----------------- |
| DUPLICATED_LOGIN_ID          | 아이디 중복            |
| DUPLICATED_EMAIL             | 이메일 중복            |
| INVALID_LOGIN_ID_OR_PASSWORD | 로그인 실패            |
| LOCKED_ACCOUNT               | 잠긴 계정             |
| WITHDRAWN_ACCOUNT            | 탈퇴 계정             |
| INVALID_TOKEN                | 잘못된 토큰            |
| EXPIRED_TOKEN                | 만료된 토큰            |
| REFRESH_TOKEN_MISMATCH       | Refresh Token 불일치 |
| USER_NOT_FOUND               | 사용자 없음            |

---

# 2. 회원가입

## POST /api/auth/signup

### Request

```json
{
  "loginId": "user01",
  "password": "password1234",
  "name": "홍길동",
  "birthDate": "2000-01-01",
  "gender": "MALE",
  "email": "user@example.com",
  "disabilities": ["발달장애"],
  "desiredJob": "사무직"
}
```

### 처리

```text
login_id 중복 확인
email 중복 확인
password 암호화
users 저장 (status = ACTIVE)
user_disabilities 저장
```

### Response

```json
{
  "userId": 1,
  "message": "회원가입이 완료되었습니다."
}
```

---

# 3. 로그인

## POST /api/auth/login

### Request

```json
{
  "loginId": "user01",
  "password": "password1234",
  "rememberMe": true
}
```

### 처리

```text
users 조회
password 검증
계정 상태 확인
last_login_at 갱신
user_disabilities 조회

Access Token 생성
Refresh Token 생성

Redis 저장
key = refresh_token:{userId}
TTL 설정
```

### Response

```json
{
  "accessToken": "access-token",
  "refreshToken": "refresh-token",
  "user": {
    "userId": 1,
    "loginId": "user01",
    "name": "홍길동",
    "email": "user@example.com",
    "disabilities": ["발달장애"],
    "desiredJob": "사무직"
  }
}
```

---

# 4. 로그아웃

## POST /api/auth/logout

### Header

Authorization: Bearer Access Token

### 처리

```text
Access Token에서 userId 추출
Redis에서 refresh_token:{userId} 삭제
이미 없어도 성공 처리
Access Token은 별도 블랙리스트 처리하지 않음
```

### Response

```json
{
  "message": "로그아웃이 완료되었습니다."
}
```

---

# 5. 토큰 재발급

## POST /api/auth/reissue

### Request

```json
{
  "refreshToken": "refresh-token"
}
```

### 처리

```text
Refresh Token 검증
userId 추출

Redis 조회
refresh_token:{userId}

요청 토큰과 Redis 값 비교

계정 상태 확인

새 Access Token 발급
(선택) Refresh Token 재발급
```

### Response

```json
{
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token"
}
```

---

# 6. 내 사용자 정보 조회

## GET /api/users/me

### 처리

```text
Access Token에서 userId 추출
users 조회
user_disabilities 조회
```

### Response

```json
{
  "userId": 1,
  "loginId": "user01",
  "name": "홍길동",
  "birthDate": "2000-01-01",
  "gender": "MALE",
  "email": "user@example.com",
  "disabilities": ["발달장애"],
  "desiredJob": "사무직",
  "accountStatus": "ACTIVE"
}
```

---

# 7. 사용자 정보 수정

## PATCH /api/users/me

### Request

```json
{
  "name": "홍길동",
  "gender": "MALE",
  "email": "new@example.com",
  "disabilities": ["발달장애"],
  "desiredJob": "단순 노무"
}
```

### 처리

```text
userId 추출
email 변경 시 중복 체크
users 수정
user_disabilities 삭제 후 재저장
트랜잭션 처리
```

### Response

```json
{
  "message": "사용자 정보가 수정되었습니다."
}
```

---

# 8. 내부 사용자 조회

## gRPC User Lookup

### 정책

* 외부 공개 API가 아니라 서비스 간 내부 통신으로만 사용한다
* MSA 내부 통신은 REST가 아니라 gRPC를 기본으로 사용한다
* 기존 `/internal/users/{userId}` REST 형태는 임시 호환 또는 로컬 검증용으로만 본다

### 처리

```text
users 조회
user_disabilities 조회
필요한 최소 사용자 정보만 반환
```

### Response Shape

```json
{
  "userId": 1,
  "loginId": "user01",
  "name": "홍길동",
  "email": "user@example.com",
  "accountStatus": "ACTIVE",
  "disabilities": ["발달장애"],
  "desiredJob": "사무직"
}
```
