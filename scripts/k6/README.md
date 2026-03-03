# Coupon k6 부하 테스트 (별도 Docker Compose)

앱용 `docker-compose.yml`와 분리해서,  
`scripts/k6/docker-compose.yml`로만 k6를 실행합니다.

## 1) 실행 방법

### 동시 발급 테스트
```bash
docker compose -f scripts/k6/docker-compose.yml run --rm k6-concurrent
```

### 단일 유저 중복 요청 테스트
```bash
docker compose -f scripts/k6/docker-compose.yml run --rm k6-duplicate
```

## 2) 자주 쓰는 커스텀 옵션

### 동시 발급 (예: 200명, couponId=1)
```bash
K6_COUPON_ID=1 K6_REQUEST_COUNT=200 \
docker compose -f scripts/k6/docker-compose.yml run --rm k6-concurrent
```

### 단일 유저 중복 (예: userId=1, 10회)
```bash
K6_COUPON_ID=1 K6_USER_ID=1 K6_DUPLICATE_REQUEST_COUNT=10 \
docker compose -f scripts/k6/docker-compose.yml run --rm k6-duplicate
```

## 3) 환경변수

- `K6_BASE_URL` (기본: `http://dragons1:8083`)
- `K6_COUPON_ID` (기본: `1`)
- `K6_REQUEST_COUNT` (기본: `200`, concurrent)
- `K6_DUPLICATE_REQUEST_COUNT` (기본: `10`, duplicate)
- `K6_USER_ID` (기본: `1`, duplicate)
- `K6_USER_ID_BASE` (기본: `1`, concurrent)
- `K6_MAX_DURATION` (기본: `30s`)

> 현재 기본 URL은 같은 compose 네트워크의 `dragons1` 컨테이너를 기준으로 합니다.
> 외부 서버를 치려면 `K6_BASE_URL`을 원하는 주소로 바꿔서 실행하세요.
