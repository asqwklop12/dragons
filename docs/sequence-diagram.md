# 구독 결제 시퀀스 다이어그램

> 전제: **세션 기반 로그인**. 결제/구독 행위는 로그인 필수.  
> 전제: Subscription은 SSOT, “진행 중”은 PaymentAttempt(PENDING)로만 표현.

---

## 1) 카드 / 토스페이 (즉시 확정 시뮬레이션)

```mermaid
sequenceDiagram
    participant U as User
    participant API as API Server
    participant PA as PaymentAttempt
    participant PE as PaymentEvent(Idempotency)
    participant S as Subscription(SSOT)
    participant Admin as Manual Event Injector

    U ->> API: 구독 요청 (CARD/TOSS)
    API ->> API: 세션 검증
    API ->> API: PENDING PaymentAttempt 존재 여부 확인(있으면 차단)
    API ->> API: ACTIVE Subscription 존재 여부 확인(있으면 차단/No-op)

    API ->> PA: PaymentAttempt 생성(status=PENDING, purpose=INITIAL)
    API -->> U: 결제 처리중(즉시 확정 대기)

    alt 성공(수동 이벤트 주입)
        Admin ->> API: PAYMENT_SUCCEEDED(attemptId)
        API ->> PE: 이벤트 저장(attemptId, PAYMENT_SUCCEEDED) (유니크로 멱등)
        API ->> PA: 상태 전이(PENDING -> SUCCEEDED)
        API ->> S: Subscription 전이(NONE/EXPIRED/CANCELED -> ACTIVE)
        API -->> U: 결제 완료 + 접근 허용(응답 또는 SSE)
    else 실패(수동 이벤트 주입)
        Admin ->> API: PAYMENT_FAILED(attemptId)
        API ->> PE: 이벤트 저장(attemptId, PAYMENT_FAILED) (유니크로 멱등)
        API ->> PA: 상태 전이(PENDING -> FAILED)
        API -->> U: 결제 실패 + 구독 미성립(Subscription 변화 없음)
    end
```

## 2) 계좌 이체 (비동기 확정 시뮬레이션)

```mermaid
sequenceDiagram
    participant U as User
    participant API as API Server
    participant PA as PaymentAttempt
    participant PE as PaymentEvent(Idempotency)
    participant S as Subscription(SSOT)
    participant Admin as Manual Event Injector

    U ->> API: 구독 요청 (ACCOUNT)
    API ->> API: 세션 검증
    API ->> API: PENDING PaymentAttempt 존재 여부 확인(있으면 차단)
    API ->> API: ACTIVE Subscription 존재 여부 확인(있으면 차단/No-op)

    API ->> PA: PaymentAttempt 생성(status=PENDING, purpose=INITIAL, expiresAt)
    API -->> U: 입금 안내(대기)

    alt 입금 성공(수동 이벤트 주입)
        Admin ->> API: PAYMENT_SUCCEEDED(attemptId)
        API ->> PE: 이벤트 저장(attemptId, PAYMENT_SUCCEEDED) (유니크로 멱등)
        API ->> PA: 상태 전이(PENDING -> SUCCEEDED)
        API ->> S: Subscription 전이(NONE/EXPIRED/CANCELED -> ACTIVE)
        API -->> U: 구독 ACTIVE + 접근 허용(응답 또는 SSE)
    else 기한 초과(배치/수동)
        Admin ->> API: PAYMENT_EXPIRED(attemptId)
        API ->> PE: 이벤트 저장(attemptId, PAYMENT_EXPIRED) (유니크로 멱등)
        API ->> PA: 상태 전이(PENDING -> EXPIRED)
        API -->> U: 만료 처리(Subscription 변화 없음)
    end
```

## 3) 재시도(중복 결제 차단 + 기존 PENDING 종료 후 재시도)

```mermaid
sequenceDiagram
    participant U as User
    participant API as API Server
    participant PA as PaymentAttempt
    participant PE as PaymentEvent(Idempotency)

    U ->> API: 재시도 요청
    API ->> API: 세션 검증
    API ->> API: 기존 PENDING PaymentAttempt 조회

    alt PENDING 존재
        API ->> PE: 이벤트 저장(attemptId, PAYMENT_TERMINATED) (멱등)
        API ->> PA: 기존 시도 종료(PENDING -> FAILED)
        API ->> PA: 새 PaymentAttempt 생성(status=PENDING)
        API -->> U: 새 시도 생성됨
    else PENDING 없음
        API ->> PA: 새 PaymentAttempt 생성(status=PENDING)
        API -->> U: 시도 생성됨
    end
```

## 4) 정기 결제(갱신) 성공/실패 흐름 (정책 미정, 전이 규칙만 고정)

```mermaid
sequenceDiagram
    participant Sch as Scheduler
    participant API as API Server
    participant PA as PaymentAttempt
    participant PE as PaymentEvent(Idempotency)
    participant S as Subscription(SSOT)
    participant Admin as Manual Event Injector

    Sch ->> API: BILLING_DUE 트리거(결제일 00:00)
    API ->> API: ACTIVE Subscription 조회
    API ->> PA: PaymentAttempt 생성(status=PENDING, purpose=RENEWAL)

    alt 갱신 성공(수동 이벤트 주입)
        Admin ->> API: PAYMENT_SUCCEEDED(attemptId)
        API ->> PE: 이벤트 저장(attemptId, PAYMENT_SUCCEEDED) (멱등)
        API ->> PA: 상태 전이(PENDING -> SUCCEEDED)
        API ->> S: Subscription 유지(ACTIVE, 연장)
    else 갱신 실패/만료(수동 이벤트 주입)
        Admin ->> API: PAYMENT_FAILED or PAYMENT_EXPIRED(attemptId)
        API ->> PE: 이벤트 저장(attemptId, eventType) (멱등)
        API ->> PA: 상태 전이(PENDING -> FAILED/EXPIRED)
        API ->> S: Subscription 전이(ACTIVE -> EXPIRED)
    end
    
```

## 5) 구독 해지 (즉시 접근 차단)

```mermaid
sequenceDiagram
    participant U as User
    participant API as API Server
    participant S as Subscription(SSOT)

    U ->> API: 해지 요청
    API ->> API: 세션 검증
    API ->> S: Subscription 전이(ACTIVE -> CANCELED)
    API -->> U: 해지 완료(즉시 접근 차단)
```

## 6) 상태 다이어그램 (Subscription)

```mermaid
stateDiagram-v2
    [*] --> NONE

    NONE --> ACTIVE: PAYMENT_SUCCEEDED(Initial)
    NONE --> NONE: PAYMENT_FAILED/EXPIRED(Initial)

    ACTIVE --> ACTIVE: PAYMENT_SUCCEEDED(Renewal)
    ACTIVE --> EXPIRED: PAYMENT_FAILED/EXPIRED(Renewal)
    ACTIVE --> CANCELED: CANCEL_REQUESTED

    EXPIRED --> ACTIVE: PAYMENT_SUCCEEDED(Re-subscribe)
    EXPIRED --> EXPIRED: PAYMENT_FAILED/EXPIRED(Re-subscribe)

    CANCELED --> ACTIVE: PAYMENT_SUCCEEDED(Re-subscribe)
    CANCELED --> CANCELED: PAYMENT_FAILED/EXPIRED(Re-subscribe)
```

## 7) 상태 다이어그램 (PaymentAttempt)

```mermaid
stateDiagram-v2
    [*] --> PENDING

    PENDING --> SUCCEEDED: PAYMENT_SUCCEEDED
    PENDING --> FAILED: PAYMENT_FAILED
    PENDING --> EXPIRED: PAYMENT_EXPIRED
    PENDING --> FAILED: PAYMENT_TERMINATED

    SUCCEEDED --> SUCCEEDED: PAYMENT_*(No-op)
    FAILED --> FAILED: PAYMENT_*(No-op)
    EXPIRED --> EXPIRED: PAYMENT_*(No-op)
```
