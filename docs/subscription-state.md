# 1. 문서 목적

결제 수단(카드/토스페이/계좌)의 완료 시점 차이를
**Subscription 상태 머신**으로 일관되게 수렴시키기 위한 기준 문서이다.

- Subscription은 권한/접근의 기준이 되는 **단일 진실 소스(SSOT)** 이다.
- 결제 흐름의 "진행 중" 상태는 Subscription이 아니라 **PaymentAttempt로 표현**한다.

---

# 2. 상태 정의 (Subscription)

| 상태 | 의미 | 접근/권한 의미 |
|---|---|---|
| `NONE` | 구독이 성립하지 않은 상태(미구독) | 유료 기능 접근 불가 |
| `ACTIVE` | 결제 성공으로 구독이 성립된 상태 | 유료 기능 접근 가능 |
| `EXPIRED` | 갱신 결제 실패/만료로 구독이 종료된 상태 | 유료 기능 접근 불가 |
| `CANCELED` | 사용자 해지로 종료된 상태 | 유료 기능 접근 불가 |

> **주의:** Subscription은 `PENDING`을 가지지 않는다.  
> "결제 대기"는 PaymentAttempt의 `PENDING`으로만 표현한다.

---

# 3. 불변식(Invariants) - 정합성 핵심 규칙

## 3.1 단일 활성 구독
- 한 사용자(userId)는 동시에 **`ACTIVE` Subscription을 1개만** 가질 수 있다.
- DB 레벨에서 `userId + (ACTIVE)` 유니크 제약으로 강제한다.

## 3.2 진행 중 결제 차단
- 사용자에게 `PENDING` PaymentAttempt가 존재하는 동안 신규 구독 신청을 허용하지 않는다.
- 즉, Subscription이 `NONE/EXPIRED/CANCELED`라도 결제 진행 중이면 신청은 차단된다.

## 3.3 이벤트 멱등성
- 결제 이벤트는 `(paymentAttemptId, eventType)` 기준으로 멱등 처리한다.
- 동일 이벤트 중복 수신 시 Subscription 상태는 한 번만 전이된다.

---

# 4. 이벤트 정의 (Subscription 관점)

| 이벤트 | 설명 |
|---|---|
| `SUBSCRIBE_REQUESTED` | 사용자가 구독 신청을 시작함 (PaymentAttempt 생성 트리거) |
| `PAYMENT_SUCCEEDED` | 결제 성공(카드/토스 즉시, 계좌는 입금 이벤트 주입) |
| `PAYMENT_FAILED` | 결제 실패 |
| `PAYMENT_EXPIRED` | 결제 대기 만료(계좌 미입금 등) |
| `CANCEL_REQUESTED` | 사용자가 구독 해지 요청 |
| `BILLING_DUE` | 정기 결제일 도래(갱신 결제 시도 생성 트리거) |

---

# 5. 상태 전이 표 (Subscription)

## 5.1 `NONE`

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `NONE` | `SUBSCRIBE_REQUESTED` | `NONE` | PaymentAttempt 생성(상태 유지) |
| `NONE` | `PAYMENT_SUCCEEDED` | `ACTIVE` | 최초 결제 성공 → 구독 성립 |
| `NONE` | `PAYMENT_FAILED` | `NONE` | 최초 결제 실패 → 구독 미성립 |
| `NONE` | `PAYMENT_EXPIRED` | `NONE` | 최초 결제 만료 → 구독 미성립 |
| `NONE` | `CANCEL_REQUESTED` | `NONE` | No-op |
| `NONE` | `BILLING_DUE` | `NONE` | 구독 없음 → No-op |

---

## 5.2 `ACTIVE`

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `ACTIVE` | `BILLING_DUE` | `ACTIVE` | 갱신 PaymentAttempt 생성(상태 유지) |
| `ACTIVE` | `PAYMENT_SUCCEEDED` | `ACTIVE` | 갱신 결제 성공(연장) |
| `ACTIVE` | `PAYMENT_FAILED` | `EXPIRED` | 갱신 결제 실패 → 즉시 만료 |
| `ACTIVE` | `PAYMENT_EXPIRED` | `EXPIRED` | 갱신 결제 만료 → 즉시 만료 |
| `ACTIVE` | `CANCEL_REQUESTED` | `CANCELED` | 사용자 해지 |
| `ACTIVE` | `SUBSCRIBE_REQUESTED` | `ACTIVE` | 이미 구독 중 → No-op(멱등 처리) |

---

## 5.3 `EXPIRED`

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `EXPIRED` | `SUBSCRIBE_REQUESTED` | `EXPIRED` | 재구독 PaymentAttempt 생성(상태 유지) |
| `EXPIRED` | `PAYMENT_SUCCEEDED` | `ACTIVE` | 재구독 성공 |
| `EXPIRED` | `PAYMENT_FAILED` | `EXPIRED` | 재구독 실패 |
| `EXPIRED` | `PAYMENT_EXPIRED` | `EXPIRED` | 재구독 만료 |
| `EXPIRED` | `CANCEL_REQUESTED` | `EXPIRED` | No-op |
| `EXPIRED` | `BILLING_DUE` | `EXPIRED` | 종료 상태 → No-op |

---

## 5.4 `CANCELED`

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `CANCELED` | `SUBSCRIBE_REQUESTED` | `CANCELED` | 재구독 PaymentAttempt 생성(상태 유지) |
| `CANCELED` | `PAYMENT_SUCCEEDED` | `ACTIVE` | 재구독 성공 |
| `CANCELED` | `PAYMENT_FAILED` | `CANCELED` | 재구독 실패 |
| `CANCELED` | `PAYMENT_EXPIRED` | `CANCELED` | 재구독 만료 |
| `CANCELED` | `CANCEL_REQUESTED` | `CANCELED` | No-op |
| `CANCELED` | `BILLING_DUE` | `CANCELED` | 해지 상태 → No-op |

---

# 6. 결제수단별 이벤트 발생 타이밍

| 결제수단 | 이벤트 발생 타이밍 |
|---|---|
| 카드 | 결제 요청 직후 `SUCCEEDED` 또는 `FAILED` |
| 토스페이 | 결제 요청 직후 `SUCCEEDED` 또는 `FAILED` |
| 계좌 | 입금 이벤트 주입 시 `SUCCEEDED`, 기한 초과 시 `EXPIRED` |

> 결제수단 차이는 "상태"가 아니라 **이벤트 발생 시점 차이**로만 모델링한다.  
> 여기서 `SUCCEEDED/FAILED/EXPIRED`는 PaymentAttempt의 상태를 의미하며, Subscription 전이는 `PAYMENT_*` 이벤트에 의해 발생한다.

---

# 7. 접근 제어(게시판) 기준

- 일반 게시판: 로그인 사용자라면 작성 가능(세션 기반)
- 유료 게시판 접근 조건: 구독 상태가 `ACTIVE`인 사용자만 접근 가능
- 프리미엄 게시판 접근 조건: 구독 상태가 `ACTIVE`이고 요금제가 `PREMIUM`인 사용자만 접근 가능

---

# 8. 구현 시 체크리스트 (정합성 관점)

- [ ] PaymentAttempt `PENDING` 중복 생성 차단
- [ ] `(paymentAttemptId, eventType)` 멱등 처리 테이블/유니크키 적용
- [ ] `ACTIVE` Subscription 유니크 제약 적용
- [ ] 이벤트 지연/중복 수신 시에도 동일 결과 유지
