# AGENTS.md

## 1. 프로젝트 개요

이 프로젝트는 장애인 맞춤형 AI 기반 기초 교육 시뮬레이션 및 구직 연계 시스템이다.

현재는 MSA 구조로 개발 중이며, 본 서비스는 user-service이다.

---

## 2. 현재 작업 범위 (User Service)

user-service의 책임은 다음과 같다.

- 회원가입 / 로그인 / 로그아웃
- JWT 기반 인증 처리
- Refresh Token 관리 (Redis)
- 사용자 기본 정보 관리
- 장애 정보(user_disabilities) 관리
- 사용자 정보 조회 및 수정
- 내부 서비스용 사용자 조회 API 제공
- 계정 상태 관리 (ACTIVE, LOCKED, WITHDRAWN)

---

## 3. 반드시 참고해야 하는 문서 (중요)

작업 전에 반드시 아래 문서를 먼저 읽을 것:

- docs/api-spec.md
- docs/database-schema.md
- docs/user-service.md

규칙:
- API 명세는 docs/api-spec.md를 기준으로 구현한다
- DB 구조는 docs/database-schema.md를 절대 기준으로 사용한다
- 기능 요구사항은 docs/user-service.md를 따른다
- 문서와 코드가 다르면 문서를 우선한다

---

## 4. 기술 스택

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA
- MySQL
- Redis (Refresh Token 저장)
- JWT
- gRPC (서비스 간 내부 통신)

---

## 5. 아키텍처 규칙

- Controller / Service / Repository 계층 분리
- Controller에는 비즈니스 로직 작성 금지
- Service에서 핵심 로직 처리
- Repository는 DB 접근만 담당
- DTO와 Entity는 반드시 분리
- Entity를 API 응답으로 직접 반환 금지
- 외부 클라이언트 대상 API는 REST를 사용한다
- 서비스 간 내부 통신은 HTTP가 아니라 gRPC를 우선 사용한다
- 내부 조회 유스케이스는 REST Controller에 종속되지 않도록 분리한다

---

## 6. 인증/인가 규칙

- 인증은 JWT 기반으로 처리한다
- Access Token은 API 인증에 사용한다
- Refresh Token은 Redis에 저장한다

Redis 정책:
```text
key = refresh_token:{userId}
TTL = Refresh Token 만료 시간
```

---

## 7. 클린 코드 규칙

- 의미가 분명한 클래스명, 메서드명, 변수명을 사용한다
- 축약어 사용은 최소화하고, 도메인 용어를 일관되게 유지한다
- 하나의 메서드는 하나의 책임만 가진다
- 메서드 길이는 가능한 짧게 유지하고, 중첩을 최소화한다
- 매직 넘버와 하드코딩 문자열은 상수 또는 설정으로 분리한다
- 예외는 무시하지 않고, 명확한 도메인 예외 또는 공통 예외로 처리한다
- null 반환보다 명시적인 예외 또는 빈 객체/컬렉션 반환을 우선한다
- 중복 로직은 공통화하되, 성급한 추상화는 피한다
- 주석은 왜 필요한지를 설명할 때만 사용하고, 코드로 의도를 드러내는 것을 우선한다
- 테스트 가능한 구조로 작성하고, 핵심 비즈니스 로직은 단위 테스트가 가능해야 한다

---

## 8. 클린 아키텍처 규칙

- 도메인 로직은 가능한 한 프레임워크와 인프라에 의존하지 않도록 작성한다
- Controller는 요청/응답 변환과 인증 사용자 식별만 담당한다
- Service는 유스케이스 단위로 동작하며, 트랜잭션 경계를 명확히 가진다
- Repository는 영속성 구현 세부사항을 캡슐화하고, 비즈니스 판단을 포함하지 않는다
- DTO는 외부 API 계약을 표현하고, Entity는 내부 도메인 상태를 표현한다
- 보안, JWT, Redis, DB 같은 인프라 관심사는 `config`, `security`, `infrastructure` 성격의 계층으로 분리한다
- 외부 기술 변경이 도메인 로직 전반에 전파되지 않도록 의존 방향을 안쪽으로 유지한다
- 서비스 간 통신이나 내부 API 보안 정책도 Controller가 아니라 애플리케이션/인프라 계층에서 처리한다
- 공통 응답, 예외 처리, 인증 처리 같은 횡단 관심사는 필터, 인터셉터, 어드바이스 등으로 분리한다
- 새로운 기능 추가 시 기존 계층 책임을 침범하지 말고, 유스케이스 중심으로 확장한다

## 9. 간단한 주석을 작성해야한다.

---

## 10. 서비스 간 통신 규칙

- MSA 내부 서비스 간 통신은 gRPC를 기본값으로 사용한다
- 내부 사용자 조회 기능은 최종적으로 gRPC 서버/클라이언트 계약으로 제공한다
- `/internal/**` 형태의 REST 엔드포인트는 임시 호환 또는 로컬 검증용으로만 사용한다
- gRPC 도입 시 비즈니스 로직은 재사용하고, 전송 계층만 별도 adapter로 분리한다
- 내부 인증/인가 정책도 장기적으로는 gRPC 호출 기준으로 설계한다
