# 1. 문서 목적

결제 수단(카드/토스페이/계좌)의 처리 방식 차이를 "결제 시도(PaymentAttempt)"로 표준화한다.

- Subscription은 권한/접근의 기준이 되는 SSOT이며, "진행 중"은 PaymentAttempt로 표현한다.
- PaymentAttempt는 **결제 1회 시도 단위의 상태 머신**이다.
- PaymentAttempt의 이벤트 처리 결과가 Subscription 전이를 트리거한다.

---

# 2. 상태 정의 (PaymentAttempt)

| 상태 | 의미 |
|---|---|
| `PENDING` | 결제 대기/진행 중 (계좌 미입금, 처리 대기) |
| `SUCCEEDED` | 결제 성공 |
| `FAILED` | 결제 실패 또는 종료(재시도/취소로 인한 종료 포함) |
| `EXPIRED` | 결제 기한 초과로 만료 |

> 본 프로젝트에서는 "사용자에 의한 취소"를 별도 상태로 두지 않고 `FAILED`로 종료 처리한다.

---

# 3. 불변식(Invariants) - 정합성 핵심 규칙
## 3.1 단일 진행 결제
- 사용자(userId) 기준으로 **동시에 `PENDING` PaymentAttempt는 1개만** 허용한다.
- `PENDING`이 존재하면 신규 구독 신청은 차단된다.
## 3.2 종료 상태는 불변
- `SUCCEEDED/FAILED/EXPIRED`는 **종료(terminal) 상태**이며, 종료 후 다른 상태로 전이하지 않는다.
## 3.3 멱등성
- 결제 이벤트는 `(paymentAttemptId, eventType)` 기준으로 멱등 처리한다.
- 동일 이벤트가 중복 수신되어도 상태 및 후속 처리(Subscription 전이)는 한 번만 발생해야 한다.

---

# 4. 이벤트 정의 (PaymentAttempt 관점)

| 이벤트 | 설명 |
|---|---|
| `PAYMENT_SUCCEEDED` | 결제 성공 |
| `PAYMENT_FAILED` | 결제 실패 |
| `PAYMENT_EXPIRED` | 결제 대기 만료(기한 초과) |
| `PAYMENT_TERMINATED` | 재시도/정책에 의해 기존 `PENDING`을 종료 처리(본 프로젝트에서는 `FAILED`로 수렴) |

> Subscription 전이는 `PAYMENT_*` 이벤트를 기반으로 수행한다.  
> 카드/토스는 요청 직후 성공/실패가 결정되며, 계좌는 입금 이벤트 주입 시 성공으로 결정된다.

---

# 5. 결제수단별 생성 시 초기 상태

| 결제수단 | PaymentAttempt 생성 직후 상태 |
|---|---|
| 카드 | `PENDING` (즉시 성공/실패 이벤트로 종료됨) |
| 토스페이 | `PENDING` (즉시 성공/실패 이벤트로 종료됨) |
| 계좌 | `PENDING` (입금 이벤트 주입 또는 만료로 종료됨) |

> 구현 상 카드/토스는 생성 직후 곧바로 `SUCCEEDED` 또는 `FAILED`로 전이된다.  
> 중요한 것은 "결제수단별 처리 방식 차이"를 상태 머신으로 흡수하는 것이다.

---

# 6. 상태 전이 표 (PaymentAttempt)

## 6.1 `PENDING`

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `PENDING` | `PAYMENT_SUCCEEDED` | `SUCCEEDED` | 결제 성공으로 종료 |
| `PENDING` | `PAYMENT_FAILED` | `FAILED` | 결제 실패로 종료 |
| `PENDING` | `PAYMENT_EXPIRED` | `EXPIRED` | 기한 초과로 만료 종료 |
| `PENDING` | `PAYMENT_TERMINATED` | `FAILED` | 재시도/정책에 의해 종료(실패 처리로 수렴) |

---

## 6.2 `SUCCEEDED` (종료 상태)

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `SUCCEEDED` | `PAYMENT_*` | `SUCCEEDED` | 종료 상태 → No-op(멱등) |

---

## 6.3 `FAILED` (종료 상태)

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `FAILED` | `PAYMENT_*` | `FAILED` | 종료 상태 → No-op(멱등) |

---

## 6.4 `EXPIRED` (종료 상태)

| 현재 | 이벤트 | 다음 | 처리/설명 |
|---|---|---|---|
| `EXPIRED` | `PAYMENT_*` | `EXPIRED` | 종료 상태 → No-op(멱등) |

---

# 7. Subscription 전이 트리거 (연동 규칙)

PaymentAttempt 이벤트가 처리될 때, 연결된 Subscription을 다음과 같이 전이시킨다.

- `PAYMENT_SUCCEEDED`
  - 신규 신청이면 Subscription을 `ACTIVE`로 성립
  - 갱신 결제이면 Subscription을 `ACTIVE` 유지(연장 처리)
- `PAYMENT_FAILED` 또는 `PAYMENT_EXPIRED`
  - 신규 신청이면 Subscription은 성립하지 않음(대개 `NONE` 유지)
  - 갱신 결제이면 Subscription을 `EXPIRED`로 전환
- `PAYMENT_TERMINATED`
  - Subscription 전이는 발생시키지 않는다. (기존 결제 시도 종료용 이벤트)

> 신규/갱신 여부는 PaymentAttempt가 "어떤 목적"으로 생성되었는지(예: INITIAL, RENEWAL)로 구분한다.

---

# 8. 동시성/중복 처리 규칙
## 8.1 PENDING 중복 생성 차단
- 동일 userId에 대해 PENDING PaymentAttempt가 있으면 새 PaymentAttempt를 생성하지 않는다.
- 재시도가 필요하다면 기존 `PENDING`을 `FAILED`로 종료 처리한 후 새 시도를 생성한다.
## 8.2 이벤트 중복/지연/순서 뒤바뀜
- 이벤트는 `(paymentAttemptId, eventType)`로 멱등 처리한다.
- `SUCCEEDED/FAILED/EXPIRED` 같은 종료 상태에 도달한 이후 도착한 이벤트는 No-op 처리한다.
- "늦게 도착한 실패 이벤트"가 이미 `SUCCEEDED`인 시도를 덮어쓰지 않도록 종료 상태 불변 규칙을 준수한다.

---

# 9. 구현 체크리스트

- [ ]  userId 기준으로 동시에 PENDING PaymentAttempt가 1개만 존재하도록 제약을 적용한다. (애플리케이션 + DB, 상태 조건부 유니크 또는 동등한 방식)
- [ ] `(paymentAttemptId, eventType)` 멱등 키 적용(유니크)
- [ ] 종료 상태 불변(terminal) 로직 적용
- [ ] PaymentAttempt 목적(INITIAL/RENEWAL) 구분 값 정의 및 저장
- [ ] PaymentAttempt 이벤트 → Subscription 전이 매핑 테스트 작성
