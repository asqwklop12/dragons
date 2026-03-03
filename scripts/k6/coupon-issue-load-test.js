import http from 'k6/http';
import { Counter, Gauge } from 'k6/metrics';

const MODE = (__ENV.MODE || 'concurrent').toLowerCase();
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const COUPON_ID = Number(__ENV.COUPON_ID || 1);
const REQUEST_COUNT = Number(__ENV.REQUEST_COUNT || (MODE === 'duplicate' ? 10 : 200));
const DUPLICATE_USER_ID = Number(__ENV.USER_ID || 1);
const USER_ID_BASE = Number(__ENV.USER_ID_BASE || 1);

const issueSuccessCounter = new Counter('issue_success');
const issueFailureCounter = new Counter('issue_failure');
const initialStockGauge = new Gauge('coupon_stock_initial');
const finalStockGauge = new Gauge('coupon_stock_final');

export const options = {
  scenarios: {
    coupon_issue_test: {
      executor: 'per-vu-iterations',
      vus: REQUEST_COUNT,
      iterations: 1,
      maxDuration: __ENV.MAX_DURATION || '30s',
    },
  },
};

function stockUrl() {
  return `${BASE_URL}/api/coupons/${COUPON_ID}/stock`;
}

function issueUrl() {
  return `${BASE_URL}/api/coupons/${COUPON_ID}/issue`;
}

function readRemainingStock() {
  const response = http.get(stockUrl(), {
    tags: { type: 'stock_check', mode: MODE },
  });

  if (response.status !== 200) {
    console.warn(`[stock] 조회 실패 status=${response.status}`);
    return null;
  }

  try {
    const body = response.json();
    return body?.data?.remainingQuantity ?? null;
  } catch (error) {
    console.warn(`[stock] 응답 파싱 실패: ${error}`);
    return null;
  }
}

function buildUserId() {
  if (MODE === 'duplicate') {
    return DUPLICATE_USER_ID;
  }

  // VU 별로 충분히 큰 간격을 둬서 충돌 없이 고유 userId 생성
  return USER_ID_BASE + ((__VU - 1) * 1_000_000) + __ITER + 1;
}

export function setup() {
  const initialStock = readRemainingStock();
  if (initialStock !== null) {
    initialStockGauge.add(initialStock);
  }

  console.log(
      `[setup] mode=${MODE}, couponId=${COUPON_ID}, requests=${REQUEST_COUNT}, initialStock=${initialStock}`,
  );

  return { initialStock };
}

export default function () {
  const userId = buildUserId();
  const response = http.post(
      issueUrl(),
      JSON.stringify({ userId }),
      {
        headers: { 'Content-Type': 'application/json' },
        tags: { type: 'issue', mode: MODE },
      },
  );

  let issued = false;
  if (response.status === 200) {
    try {
      const body = response.json();
      issued = body?.meta?.result === 'SUCCESS';
    } catch (error) {
      issued = false;
    }
  }

  if (issued) {
    issueSuccessCounter.add(1);
    return;
  }

  issueFailureCounter.add(1, { status: String(response.status) });
}

export function teardown(data) {
  const finalStock = readRemainingStock();
  if (finalStock !== null) {
    finalStockGauge.add(finalStock);
  }

  console.log(`[teardown] initialStock=${data.initialStock}, finalStock=${finalStock}`);
}

function readMetricCount(data, metricName) {
  return Number(data.metrics?.[metricName]?.values?.count ?? 0);
}

function readGaugeValue(data, metricName) {
  const value = data.metrics?.[metricName]?.values?.value;
  return value === undefined ? null : Number(value);
}

export function handleSummary(data) {
  const success = readMetricCount(data, 'issue_success');
  const failure = readMetricCount(data, 'issue_failure');
  const initialStock = readGaugeValue(data, 'coupon_stock_initial');
  const finalStock = readGaugeValue(data, 'coupon_stock_final');

  let expectationLine = '';
  if (MODE === 'concurrent' && initialStock !== null) {
    const expectedSuccess = Math.min(initialStock, REQUEST_COUNT);
    expectationLine = `- 기대 성공 수(동시 발급): min(${initialStock}, ${REQUEST_COUNT}) = ${expectedSuccess}`;
  }
  if (MODE === 'duplicate') {
    expectationLine = '- 기대 성공 수(단일 유저 중복): 보통 1회 (사전 발급 이력 없고 재고가 충분한 경우)';
  }

  const summary = [
    '',
    '=== Coupon Issue Load Test Summary ===',
    `- mode: ${MODE}`,
    `- baseUrl: ${BASE_URL}`,
    `- couponId: ${COUPON_ID}`,
    `- requestCount: ${REQUEST_COUNT}`,
    `- success: ${success}`,
    `- failure: ${failure}`,
    `- initialStock: ${initialStock}`,
    `- finalStock: ${finalStock}`,
    expectationLine,
    '======================================',
    '',
  ].join('\n');

  return {
    stdout: summary,
  };
}
