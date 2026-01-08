# 구독 서비스

## 목표
카드 / 토스페이 / 계좌처럼 **결제 완료 시점이 다른 결제수단**을 하나의 **상태 머신**으로 수렴시켜 **일관된 상태**를 보장한다.

- 결제는 “돈 처리”가 아니라 **상태 전이 트리거**
- **Subscription이 SSOT(권한/접근의 기준)**
- “진행 중”은 Subscription이 아니라 **PaymentAttempt(PENDING)** 로만 표현

---

## 인증/인가 정책 (세션 기반)
- 로그인 방식: **세션**
- 프리뷰(공개) 콘텐츠: **비로그인 접근 허용**
- 결제/구독 행위: **로그인 필수**
- 로그아웃: 세션 무효화 → **프리미엄 접근 즉시 차단**
- 인가(Authorization): 로그인 여부가 아니라 **구독 상태(Subscription)** 로 결정

---

## 멀티 모듈 구조

```
Root  
├── apps ( spring-applications )  
│   ├── 📦 dragons-api
├── 📦 domain ( reusable-configurations )
└── infra ( add-ons )  
    ├── 📦 jackson  
    ├── 📦 jpa
    ├── 📦 jwt
    ├── 📦 logging
    └── 📦 redis
```

---

## 도메인 상태 모델

### Subscription (SSOT)
- 상태: `NONE`, `ACTIVE`, `EXPIRED`, `CANCELED`
- 주의: **Subscription에는 `PENDING`이 없다**
    - 결제 대기는 **PaymentAttempt의 `PENDING`** 으로만 표현

### PaymentAttempt (결제 1회 시도)
- 상태: `PENDING`, `SUCCEEDED`, `FAILED`, `EXPIRED`
- `SUCCEEDED/FAILED/EXPIRED`는 **terminal(종료) 상태** → 이후 다른 상태로 전이 불가
- 사용자 취소는 별도 상태 없이 **`FAILED`로 종료 처리**
- 목적(purpose): `INITIAL`(최초) / `RENEWAL`(갱신)

---

## 결제수단 처리 규칙

### 카드 / 토스페이 (즉시 확정)
- PaymentAttempt 생성: `PENDING`
- 요청 직후:
    - 성공: `SUCCEEDED` → Subscription 전이
    - 실패: `FAILED` → Subscription 전이(또는 유지)

### 계좌 이체 (비동기 확정)
- PaymentAttempt 생성: `PENDING` (만료 기한 포함 가능)
- 입금 이벤트 수동 주입 시: `SUCCEEDED`
- 기한 초과 시: `EXPIRED`
- **가상계좌 발급 없음**
- “입금 확인”은 **DB 변경/이벤트 주입으로 시뮬레이션**

---

## 이벤트/멱등 처리
- 결제 결과는 **이벤트 기반 처리**
- 멱등키: **(paymentAttemptId, eventType)**
- 동일 이벤트 중복 수신 시:
    - 상태 전이는 **1회만**
    - terminal 상태 이후 도착 이벤트는 **No-op**
    - 늦게 도착한 실패 이벤트가 이미 `SUCCEEDED`를 **덮어쓰지 못함**

---

## 동시성/중복 처리 규칙
- 사용자(userId) 기준:
    - `ACTIVE` Subscription은 **동시에 1개**
    - `PENDING` PaymentAttempt는 **동시에 1개**
- `PENDING` PaymentAttempt가 존재하면:
    - 신규 구독 신청/새 결제 시도 **차단**
- 재시도 정책:
    - 기존 `PENDING`은 삭제하지 않고 **종료 처리(FAILED)** 후 새 시도 생성

---

## Subscription 전이 규칙 (PaymentAttempt 이벤트 연동)

### PAYMENT_SUCCEEDED
- INITIAL: Subscription → `ACTIVE` (구독 성립)
- RENEWAL: Subscription `ACTIVE` 유지(연장)

### PAYMENT_FAILED / PAYMENT_EXPIRED
- INITIAL: Subscription 변화 없음(대개 `NONE` 유지)
- RENEWAL: Subscription → `EXPIRED`

### PAYMENT_TERMINATED
- Subscription 전이 없음 (기존 `PENDING` 종료용)

---

## 접근 제어(콘텐츠)
- 유료 접근 조건: `Subscription.state == ACTIVE`
- 프리미엄 추가 조건: `Subscription.state == ACTIVE && Subscription.plan == PREMIUM`
- 접근 제어는 **서버 기준** (프론트 신뢰 금지)

---

## Audit / Soft Delete (전 엔티티 공통)
모든 엔티티에 아래 컬럼을 포함한다.
- `created_at` (not null, updatable=false)
- `updated_at` (not null)
- `deleted_at` (nullable)

기본 조회/권한 판단 기준:
- `deleted_at IS NULL`

---

## 엔티티 네이밍/필드

### users
- `name` (not null, length 100)
- `email` (not null, unique)
- `password` NULL 가능 (Google OAuth 사용자)
- Google OAuth 지원(정책: OAuth 성공 ≠ 로그인, 최초 OAuth 시 REGISTERED 생성)

### posts
- `title` (not null)
- `content` (not null, length 10000)
- `category` (not null)
- `author` (not null)
- `is_public` (not null)

---

## 스케줄러(정기 결제)
- 정기 결제일 산정 방식: **정책 미정**
- 배치 처리 기준 시점: **결제일 00:00 고정**
- 결제일 계산 로직은 **교체 가능**해야 한다
- 갱신 결제가 `FAILED` 또는 `EXPIRED`면 Subscription → `EXPIRED`

---

## 범위 제외
- 실제 결제 PG 연동
- 가상계좌 발급/관리
- 은행 코드 검증
- 정산/환불
- 잔액/원장 관리
