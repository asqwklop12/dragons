# k6 payment demo

nginx 로드밸런서 앞단을 통해 토스 결제 생성/성공 콜백 흐름을 재현하는 k6 스크립트입니다.

## 목적

- nginx 뒤의 `dragons1~3` 인스턴스로 트래픽을 분산시킵니다.
- `/api/payments/toss` 주문 생성 후 `/api/payments/toss/success` 콜백을 호출합니다.
- 같은 `orderId`에 대해 성공 콜백을 **동시에 여러 번** 보내 결제 확인 분산락 경합을 의도적으로 만듭니다.
- Prometheus / Grafana 의 `redis_lock_*` 메트릭이 의미 있게 쌓이도록 합니다.

## 파일

- 스크립트: `scripts/toss-payment-nginx-demo.js`
- 실행용 Compose: `docker-compose.yml`
- 기본 진입점
  - 호스트 로컬 기준: `https://localhost`
  - Docker Compose 실행 기준: `https://host.docker.internal`
- TLS: 로컬 nginx 인증서를 고려해 `insecureSkipTLSVerify: true` 적용
- 기본 실행 시간: **30분**

## 기본 30분 베이스라인 프로파일

기본값은 `TEST_PROFILE=baseline_30m`, `MAX_VUS=1000` 입니다.

30분 동안 점진적으로 부하를 올렸다가 내리는 램프 프로파일입니다.

기본 stage 는 아래와 같습니다.

1. 5분 → `MAX_VUS`의 33%
2. 5분 → `MAX_VUS`의 67%
3. 5분 → `MAX_VUS`의 100%
4. 5분 → `MAX_VUS`의 100% 유지
5. 5분 → `MAX_VUS`의 50%
6. 5분 → `0 VUs`

> `MAX_VUS` 를 바꾸면 각 target 이 비율로 같이 바뀝니다.

## 요청 흐름

### 1) 주문 생성

- `POST /api/payments/toss`
- 헤더: `X-Dev-User-Email`
- 본문:

```json
{
  "amount": 9900,
  "orderName": "구독",
  "customerName": "랜덤 이름",
  "planType": "premium"
}
```

### 2) 성공 콜백

- `GET /api/payments/toss/success?paymentKey=...&orderId=...&amount=...`
- 헤더: `X-Dev-User-Email`
- `orderId`, `amount` 는 1단계 응답값 사용
- 기본적으로 같은 주문에 대해 `CONFIRM_BATCH_SIZE=3` 번 동시 호출
  - 기대 결과: 보통 1건 성공 + 나머지 409(conflict)

> 코드상 `/api/payments/toss/success` 도 전역 로그인 인터셉터를 타므로, dev/local 에서는 `X-Dev-User-Email` 헤더를 같이 보내는 편이 안전합니다.

## 실행 방법

```bash
cd monitoring/k6
docker compose run --rm k6
```

이 명령은 k6 컨테이너 안에서 스크립트를 실행합니다.
기본 `BASE_URL` 은 `https://host.docker.internal` 이므로, 호스트에 바인딩된 nginx(`https://localhost`)로 우회 접속합니다.
기본 프로파일은 **30분 베이스라인 테스트**입니다.

## 자주 쓸 옵션

### VU / 배치 조정

```bash
cd monitoring/k6
MAX_VUS=800 \
CONFIRM_BATCH_SIZE=4 \
docker compose run --rm k6
```

### 기존 30분 램프 테스트로 되돌리기

```bash
cd monitoring/k6
TEST_PROFILE=baseline_30m \
MAX_VUS=90 \
docker compose run --rm k6
```

### 10분 빠른 확인 프로파일

```bash
cd monitoring/k6
TEST_PROFILE=quick_10m \
MAX_VUS=90 \
docker compose run --rm k6
```

`quick_10m` stage:

1. 2분 → `MAX_VUS`의 40%
2. 6분 → `MAX_VUS`의 70%
3. 2분 → `0 VUs`

### stage 직접 오버라이드

```bash
cd monitoring/k6
STAGES_JSON='[
  {"duration":"3m","target":20},
  {"duration":"7m","target":60},
  {"duration":"10m","target":120},
  {"duration":"5m","target":60},
  {"duration":"5m","target":0}
]' \
docker compose run --rm k6
```

### BASE_URL 강제 지정

```bash
cd monitoring/k6
BASE_URL=https://host.docker.internal docker compose run --rm k6
```

## 지원 환경 변수

- `BASE_URL` (기본: `https://host.docker.internal`)
- `TEST_PROFILE` (기본: `baseline_30m`, 지원: `quick_10m`, `spike_3h`, `baseline_30m`)
- `MAX_VUS` (기본: `1000`)
- `CONFIRM_BATCH_SIZE` (기본: `3`)
- `AMOUNT` (기본: `9900`)
- `ORDER_NAME` (기본: `구독`)
- `PLAN_TYPE` (기본: `premium`)
- `EMAIL_DOMAIN` (기본: `k6.local`)
- `THINK_TIME_MIN` (기본: `0.2`)
- `THINK_TIME_MAX` (기본: `1.0`)
- `UNEXPECTED_SAMPLE_LIMIT` (기본: `10`, 비정상 confirm 응답 샘플 로그 개수)
- `STAGES_JSON` (기본 stage override)

## k6에서 바로 보기 쉬운 추가 지표

이번 스크립트는 confirm 분석을 쉽게 하려고 아래 커스텀 메트릭도 같이 출력합니다.

- `confirm_success_count`
- `confirm_conflict_count`
- `confirm_other_4xx_count`
- `confirm_5xx_count`
- `confirm_other_status_count`
- `confirm_unexpected_count`
- `confirm_duration`
- `confirm_unexpected_duration`

비정상 confirm 응답이 나오면 stderr에 샘플 로그를 남깁니다.

예시:

```text
[confirm-unexpected] status=500 vu=12 iter=345 orderId=... paymentKey=... duration_ms=123 body=...
```

샘플 로그 수는 `UNEXPECTED_SAMPLE_LIMIT` 으로 제한합니다.

## 관측 포인트

Grafana `Redis Distributed Lock` 대시보드는 아래 기준으로 읽으면 됩니다.

- `락 획득 시도 (초당)`: 인스턴스별 락 시도량
- `락 경합률 (인스턴스별)`: 시도 대비 실패 비율
- `락 실행 시간 p50 / p95 / p99`: Micrometer histogram 활성화 후 분위수 패널로 확인
- `락 해제 성공 / 실패 (초당)`: 해제가 정상적으로 따라오는지 확인
- `HTTP 4XX / 5XX 에러 (초당)`: 전체 애플리케이션 기준 에러 추이 확인
- `해제 성공 횟수 (누적)`: `락 획득 성공`과 비슷하게 증가하는지 확인


> `common/src/main/resources/monitoring.yml` 에 `management.metrics.distribution.percentiles-histogram.redis.lock.execution.duration=true` 와 SLO bucket 을 추가해 `_bucket` 시계열이 Prometheus 로 노출되도록 맞췄습니다. 앱을 재기동해야 패널에 값이 뜹니다.

Prometheus / Grafana 에서 우선 보면 좋은 메트릭:

- `redis_lock_acquire_attempts_total`
- `redis_lock_acquire_success_total`
- `redis_lock_acquire_failures_total`
- `redis_lock_execution_duration_seconds_*`
- `redis_lock_release_success_total`
- `redis_lock_release_failures_total`

### Prometheus 예시 쿼리

```promql
sum by (instance) (rate(redis_lock_acquire_attempts_total[1m]))
```

```promql
sum by (instance) (rate(redis_lock_acquire_failures_total[1m]))
/
sum by (instance) (rate(redis_lock_acquire_attempts_total[1m]))
```

```promql
sum(rate(http_server_requests_seconds_count{status=~"4.."}[1m]))
```

```promql
sum(rate(http_server_requests_seconds_count{status=~"5.."}[1m]))
```

## 참고

- 이 스크립트는 개별 앱 포트를 직접 치지 않고 **nginx 진입점만** 사용합니다.
- Docker Compose 안의 `localhost` 는 k6 컨테이너 자신이라서, 기본값을 `host.docker.internal` 로 잡았습니다.
- `9797` host mapping 없이도 Prometheus 수집에는 문제없고, 이 스크립트도 actuator 포트를 직접 사용하지 않습니다.

### 추가로 직접 보면 좋은 쿼리

```promql
sum by (instance) (rate(redis_lock_acquire_success_total[1m]))
```

```promql
sum by (instance) (rate(redis_lock_release_success_total[1m]))
```

```promql
sum(rate(redis_lock_release_failures_total[1m]))
```
