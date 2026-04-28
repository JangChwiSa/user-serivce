# User Service Research

## 1. 프로젝트 요약

이 저장소는 `Didgo` 시스템의 `user-service`이며, 현재 `main` 브랜치는 Spring Boot 기반의 인증/사용자 관리 서비스 1차 구현 상태다.

현재 `main`이 제공하는 핵심 기능:

- 회원가입
- 로그인
- 로그아웃
- Access/Refresh Token 기반 인증
- 내 정보 조회
- 내 정보 수정
- 내부 사용자 조회 REST API
- Swagger UI 노출
- Docker 빌드/런타임 기본 설정

현재 `main`에는 아직 포함되지 않은 별도 브랜치:

- `feat/grpc-internal-user-service`
  - 내부 사용자 조회를 gRPC로 확장한 작업 브랜치

즉, `main`은 REST 중심의 안정화 브랜치이고, gRPC 전환은 별도 기능 브랜치에 분리되어 있다.

## 2. 현재 아키텍처

구조는 전형적인 계층형 Spring 애플리케이션이다.

- `controller`
  - HTTP 요청/응답 처리
- `service`
  - 유스케이스와 비즈니스 흐름 처리
- `repository`
  - JPA 영속성 접근
- `domain`
  - `User`, `UserDisability`, enum
- `dto`
  - API 요청/응답 계약
- `security`
  - JWT 필터, 내부 API 키 필터, Security 설정
- `common`
  - 공통 예외, 에러 응답
- `config`
  - 설정 프로퍼티, Clock, OpenAPI

설계 의도 자체는 비교적 명확하다.

- Controller는 thin하게 유지됨
- Entity와 DTO가 분리됨
- 토큰 처리와 Redis 저장이 별도 서비스로 분리됨
- 시간 생성은 `Clock` 빈으로 추상화되어 테스트 가능성이 확보됨

## 3. 도메인 모델

### 3.1 User

`[User.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/user/domain/User.java)` 는 다음 속성을 가진다.

- `userId`
- `loginId`
- `passwordHash`
- `name`
- `birthDate`
- `gender`
- `email`
- `desiredJob`
- `status`
- `createdAt`
- `updatedAt`
- `lastLoginAt`

도메인 메서드:

- `create(...)`
- `updateProfile(...)`
- `updateLastLoginAt(...)`
- `changeStatus(...)`

평가:

- 생성과 수정 메서드가 분리되어 있어 흐름 파악이 쉽다
- 상태 변경용 메서드가 있어 계정 정책 확장 가능성이 있다
- 하지만 엔티티 자체에 비즈니스 불변식 검증은 거의 없다

### 3.2 UserDisability

`[UserDisability.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/user/domain/UserDisability.java)` 는 `user` 와 `disabilityType` 중심의 단순 모델이다.

평가:

- 현재 API 명세의 `disabilities: [String]` 구조와 잘 맞는다
- 하지만 `user-service.md` 에 적힌 `장애 정도`, `보조 필요사항`, `특이사항` 과는 맞지 않는다

## 4. 인증/인가 구조

핵심 파일:

- `[SecurityConfig.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/security/SecurityConfig.java)`
- `[JwtAuthenticationFilter.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/security/JwtAuthenticationFilter.java)`
- `[InternalApiKeyFilter.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/security/InternalApiKeyFilter.java)`
- `[JwtTokenProvider.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/security/JwtTokenProvider.java)`

동작 방식:

- 세션/폼 로그인/HTTP Basic 모두 비활성화
- `/api/auth/signup`, `/api/auth/login`, `/api/auth/reissue` 공개
- `/api/**` 는 JWT 인증 필요
- `/internal/**` 는 별도 내부 API 키 필요
- JWT 인증 성공 시 `AuthenticatedUser(userId)` 를 SecurityContext에 넣음

장점:

- REST API 기준으로는 단순하고 이해하기 쉽다
- Access Token/Refresh Token 타입을 claim으로 분리해 오용 가능성을 줄였다
- 에러 응답을 JSON으로 일관되게 반환한다

주의점:

- `main` 기준 내부 서비스 통신은 여전히 HTTP + API Key다
- MSA 내부 통신을 gRPC로 가져가려는 방향과는 현재 `main`이 어긋난다
- `JWT_SECRET` 기본값이 설정 파일에 fallback으로 들어 있어 운영에서는 반드시 환경변수 강제가 필요하다

## 5. 인증 유스케이스 분석

핵심 파일:

- `[AuthService.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/auth/service/AuthService.java)`
- `[RefreshTokenService.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/auth/service/RefreshTokenService.java)`

### 5.1 회원가입

흐름:

- `loginId` 중복 확인
- `email` 중복 확인
- 비밀번호 암호화
- `User` 저장
- 장애 정보 저장

평가:

- 기본 흐름은 명세와 일치
- `disabilities` 중복 입력을 `distinct()` 로 정리하는 점은 좋다

### 5.2 로그인

흐름:

- `loginId` 로 사용자 조회
- 비밀번호 검증
- 계정 상태 검증
- `lastLoginAt` 갱신
- Access/Refresh Token 발급
- Refresh Token Redis 저장
- 사용자 요약 응답 반환

평가:

- 문서 기준 핵심 요구를 충족
- `rememberMe` 에 따라 Refresh Token 만료시간 분기 처리도 있음

### 5.3 로그아웃

흐름:

- 현재 사용자 기준 Redis Refresh Token 삭제

평가:

- 명세와 일치
- Access Token 블랙리스트는 구현하지 않음

### 5.4 재발급

흐름:

- Refresh Token JWT 검증
- 토큰에서 `userId` 추출
- Redis 저장값과 일치 여부 확인
- 계정 상태 재검증
- 새 Access Token 발급
- 새 Refresh Token 발급 및 Redis 갱신

평가:

- 서버 저장값 대조를 통해 탈취/재사용 위험을 일부 완화한다
- 다만 현재 구현은 `rememberMe` 상태를 재발급에 유지하지 않는다
  - 재발급 시 항상 `false` 기준 만료시간으로 새 Refresh Token을 만든다

## 6. 사용자 API 분석

핵심 파일:

- `[UserService.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/user/service/UserService.java)`
- `[UserController.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/user/controller/UserController.java)`
- `[InternalUserController.java](/Users/byeok27/Documents/GitHub/user-serivce/src/main/java/com/didgo/userservice/user/controller/InternalUserController.java)`

### 6.1 내 정보 조회

응답은 `loginId`, `name`, `birthDate`, `gender`, `email`, `disabilities`, `desiredJob`, `accountStatus` 를 포함한다.

평가:

- API 명세와 대체로 일치
- `gender` 를 문자열로 직렬화하는 단순한 구조다

### 6.2 내 정보 수정

흐름:

- 사용자 조회
- 이메일 중복 검사
- 기본 정보 수정
- 기존 장애 정보 전체 삭제
- 새 장애 정보 재저장

평가:

- 트랜잭션 경계가 명확하다
- 장애 정보는 patch가 아니라 replace semantics다

### 6.3 내부 사용자 조회

현재 `main` 에서는 `/internal/users/{userId}` REST 엔드포인트로 제공된다.

평가:

- 내부 서비스에서 필요한 최소 정보 반환이라는 의도는 분명하다
- 하지만 gRPC 전환 계획과는 충돌한다
- 현재 브랜치 구조상 gRPC 버전은 `feat/grpc-internal-user-service` 에만 존재한다

## 7. 테스트 상태

현재 테스트는 서비스 레이어 중심이다.

파일:

- `[AuthServiceTest.java](/Users/byeok27/Documents/GitHub/user-serivce/src/test/java/com/didgo/userservice/auth/service/AuthServiceTest.java)`
- `[UserServiceTest.java](/Users/byeok27/Documents/GitHub/user-serivce/src/test/java/com/didgo/userservice/user/service/UserServiceTest.java)`

포함되는 검증:

- 회원가입 시 비밀번호 암호화
- 로그인 성공
- 잠긴 계정 로그인 실패
- Refresh Token 불일치 시 재발급 실패
- 로그아웃 시 Redis 삭제 호출
- 내 정보 조회
- 내 정보 수정
- 중복 이메일 수정 실패
- 내부 사용자 조회

장점:

- 비즈니스 로직 단위 검증은 잘 되어 있음
- `Clock.fixed(...)` 를 사용해 시간 관련 테스트를 안정화함

부족한 점:

- Controller/WebMvc 테스트 없음
- Security 필터 통합 테스트 없음
- JPA 매핑 테스트 없음
- 실제 Redis/MySQL 통합 테스트 없음
- Swagger/OpenAPI 노출 테스트 없음
- Docker 빌드/실행 검증 없음

## 8. 설정 및 실행 환경

### 8.1 애플리케이션 설정

`[application.yml](/Users/byeok27/Documents/GitHub/user-serivce/src/main/resources/application.yml)` 기준:

- DB 기본값은 로컬 MySQL
- Redis 기본값은 로컬 `localhost:6379`
- JPA `ddl-auto: update`
- Hibernate SQL debug 활성화
- Swagger UI 경로 `/swagger-ui.html`

평가:

- 개발 편의성은 높다
- 운영 기준으로는 위험 요소가 있다

운영 리스크:

- `ddl-auto: update` 는 운영 DB에 부적절할 가능성이 높다
- 기본 `JWT_SECRET` fallback 값 존재
- 기본 DB 비밀번호가 `password`
- 내부 API 키도 기본값 존재

### 8.2 Docker

파일:

- `[Dockerfile](/Users/byeok27/Documents/GitHub/user-serivce/Dockerfile)`
- `[.dockerignore](/Users/byeok27/Documents/GitHub/user-serivce/.dockerignore)`

특징:

- 멀티스테이지 빌드 사용
- JDK 21로 빌드 후 JRE 21 이미지로 실행
- 테스트는 건너뛰고 패키징

평가:

- 기본 컨테이너 실행에는 충분하다
- 하지만 운영용 헬스체크, non-root 사용자, 환경별 프로파일 전략은 없다

## 9. 문서와 코드의 일치도

현재 저장소의 가장 큰 문제는 문서 일관성이 완전히 닫혀 있지 않다는 점이다.

### 9.1 일치하는 부분

- 로그인 식별자를 `loginId` 로 사용하는 실제 구현
- JWT + Redis Refresh Token 구조
- `users`, `user_disabilities` 분리 구조
- 계정 상태 `ACTIVE`, `LOCKED`, `WITHDRAWN`
- 사용자 수정 시 장애 정보 전체 교체

### 9.2 불일치하는 부분

#### `docs/user-service.md` 와 실제 코드

- 문서는 로그인 기준이 `이메일/비밀번호` 로 서술됨
- 실제 구현은 `loginId/비밀번호`

- 문서는 `phone`, `withdrawnAt`, `disabilityDegree`, `supportNeeds`, `notes` 등을 암시
- 실제 DB/코드는 해당 필드 없음

- 문서는 내부 API를 `/internal/users/{userId}` 로 설명
- 브랜치 전략상 gRPC 전환을 별도로 진행 중이나 `main`에는 반영되지 않음

#### `AGENTS.md` 와 현재 `main`

- `AGENTS.md` 는 gRPC 규칙을 강하게 요구하는 방향으로 정리됐을 수 있으나, 현재 `main`은 gRPC 구현을 포함하지 않음
- 즉 현재 `main`은 문서보다 한 단계 이전의 구현 상태로 볼 수 있다

## 10. 현재 브랜치 해석

현재 Git 상태:

- `main`
  - Swagger + Docker 반영, REST 중심 안정화 브랜치
- `feat/project-bootstrap`
  - 초기 부트스트랩
- `feat/auth-token-management`
  - JWT/Redis 인증 구현
- `feat/user-profile-apis`
  - 사용자 API와 주석 추가
- `feat/grpc-internal-user-service`
  - gRPC 내부 조회 확장

실무적으로는 이 해석이 가장 맞다.

- `main`은 현재 배포 후보
- `feat/grpc-internal-user-service` 는 다음 단계 기능 브랜치

즉, 지금 저장소는 “REST 기반 user-service 1차 구현”과 “gRPC 전환 2차 작업”이 브랜치로 분리된 상태다.

## 11. 강점

- 구조가 단순하고 파악이 쉽다
- 비즈니스 로직과 HTTP 레이어가 분리돼 있다
- JWT/Refresh Token 처리 흐름이 명확하다
- 테스트 가능성을 고려해 `Clock` 주입 구조를 도입했다
- 서비스 레이어 테스트 커버리지가 최소 수준은 확보돼 있다
- Swagger와 Docker가 추가되며 개발 편의성이 향상됐다

## 12. 주요 리스크

### 12.1 문서 드리프트

가장 큰 리스크다.

- `api-spec.md`
- `user-service.md`
- `AGENTS.md`
- 실제 `main`

이 네 축이 완전히 일치하지 않는다.

### 12.2 내부 통신 방식 미정착

- `main`은 내부 REST
- 별도 브랜치는 gRPC

이 상태가 길어지면 다른 서비스가 어느 계약을 따라야 할지 불명확해진다.

### 12.3 운영 설정 안전성 부족

- `ddl-auto: update`
- fallback 비밀값
- 내부 API 키 기본값

운영 환경 투입 전 정리 필요

### 12.4 테스트 범위 제한

- 통합 테스트 부재
- 보안 필터 테스트 부재
- 실제 Redis/MySQL 연동 테스트 부재

### 12.5 내부 API 보안 단순화

- 현재 `/internal/**` 는 단순 API Key 헤더 방식
- 서비스 메시 환경, mTLS, gRPC 인증 등 장기 설계로 보기엔 약하다

## 13. 추천 우선순위

### 1순위: 기준 문서 정리

먼저 하나를 확정해야 한다.

- `main` 기준으로 문서를 맞출지
- `gRPC 브랜치` 기준으로 차기 명세를 맞출지

### 2순위: 인프라 연결 검증

- MySQL 실제 연결
- Redis 실제 연결
- 애플리케이션 기동
- Swagger 확인
- 회원가입/로그인/재발급 수동 테스트

### 3순위: 내부 통신 전략 확정

둘 중 하나를 명확히 해야 한다.

- 단기: `/internal/**` 유지
- 중기: gRPC 브랜치 머지

### 4순위: 운영 준비

- Flyway/Liquibase 도입
- 환경변수 강제
- 비밀값 fallback 제거
- Docker 실행 전략 개선

### 5순위: 테스트 확장

- Controller/WebMvc 테스트
- Security 통합 테스트
- Repository/JPA 테스트
- MySQL/Redis 통합 테스트

## 14. 결론

이 프로젝트는 현재 “기능 구현 자체는 충분히 진행된 user-service”다. 특히 인증과 사용자 관리의 기본 뼈대는 이미 usable한 수준이다.

다만 문제의 중심은 코드 품질보다 `기준의 분산`이다.

- 문서가 서로 다름
- `main`과 `gRPC` 방향이 분리돼 있음
- 운영 기준 설정이 아직 개발 친화 상태임

따라서 다음 단계의 핵심은 새로운 기능 추가보다 아래 세 가지다.

1. 어떤 브랜치를 기준 구현으로 삼을지 확정
2. MySQL/Redis 실연결과 수동 테스트 완료
3. 내부 통신 전략을 REST 유지 또는 gRPC 전환 중 하나로 닫기

현재 `main`만 기준으로 보면:

- REST 기반 user-service 1차 구현은 완료에 가깝다
- 실환경 연결과 문서 정리가 남아 있다

현재 저장소 전체를 기준으로 보면:

- gRPC 전환까지 포함한 구조 개편은 아직 진행 중이다
