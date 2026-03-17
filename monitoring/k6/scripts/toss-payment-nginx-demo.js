import http from 'k6/http';
import exec from 'k6/execution';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

const BASE_URL = (__ENV.BASE_URL || 'https://localhost').replace(/\/$/, '');
const AMOUNT = Number(__ENV.AMOUNT || 9900);
const ORDER_NAME = __ENV.ORDER_NAME || '구독';
const PLAN_TYPE = __ENV.PLAN_TYPE || 'premium';
const EMAIL_DOMAIN = __ENV.EMAIL_DOMAIN || 'k6.local';
const CONFIRM_BATCH_SIZE = Math.max(1, Number(__ENV.CONFIRM_BATCH_SIZE || 3));
const MAX_VUS = Math.max(1, Number(__ENV.MAX_VUS || 90));
const THINK_TIME_MIN = Number(__ENV.THINK_TIME_MIN || 0.2);
const THINK_TIME_MAX = Number(__ENV.THINK_TIME_MAX || 1.0);

const createExpected = http.expectedStatuses(200);
const confirmExpected = http.expectedStatuses(200, 409);

const orderCreateSuccessRate = new Rate('order_create_success_rate');
const confirmBatchHasSuccessRate = new Rate('confirm_batch_has_success_rate');
const confirmUnexpectedRate = new Rate('confirm_unexpected_rate');
const confirmSuccessCount = new Counter('confirm_success_count');
const confirmConflictCount = new Counter('confirm_conflict_count');
const paymentFlowDuration = new Trend('payment_flow_duration', true);

function stageTarget(ratio) {
  return Math.max(1, Math.round(MAX_VUS * ratio));
}

function buildStages() {
  if (__ENV.STAGES_JSON) {
    return JSON.parse(__ENV.STAGES_JSON);
  }

  return [
    { duration: '5m', target: stageTarget(1 / 3) },
    { duration: '5m', target: stageTarget(2 / 3) },
    { duration: '5m', target: stageTarget(1.0) },
    { duration: '5m', target: stageTarget(1.0) },
    { duration: '5m', target: stageTarget(0.5) },
    { duration: '5m', target: 0 },
  ];
}

export const options = {
  insecureSkipTLSVerify: true,
  scenarios: {
    toss_payment_nginx_demo: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: buildStages(),
      gracefulRampDown: '30s',
    },
  },
  thresholds: {
    order_create_success_rate: ['rate>0.99'],
    confirm_batch_has_success_rate: ['rate>0.99'],
    confirm_unexpected_rate: ['rate==0'],
    payment_flow_duration: ['p(95)<5000'],
  },
};

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function thinkTime() {
  return THINK_TIME_MIN + Math.random() * Math.max(THINK_TIME_MAX - THINK_TIME_MIN, 0);
}

function randomCustomerName() {
  const familyNames = ['김', '이', '박', '최', '정', '강', '조', '윤'];
  const givenNames = ['민준', '서준', '도윤', '서연', '지우', '하준', '예준', '지안'];
  return `${familyNames[randomInt(0, familyNames.length - 1)]}${givenNames[randomInt(0, givenNames.length - 1)]}${randomInt(10, 99)}`;
}

function buildEmail() {
  const iteration = exec.scenario.iterationInTest;
  return `payment-${exec.vu.idInTest}-${iteration}-${randomInt(1000, 9999)}@${EMAIL_DOMAIN}`;
}

function parseJson(response) {
  try {
    return response.json();
  } catch (_) {
    return null;
  }
}

function unwrapPayload(payload) {
  if (payload && typeof payload === 'object' && 'data' in payload) {
    return payload.data;
  }
  return payload;
}

function buildConfirmUrl(successUrl, paymentKey, orderId, amount) {
  const base = successUrl || `${BASE_URL}/api/payments/toss/success`;
  return `${base}?paymentKey=${encodeURIComponent(paymentKey)}&orderId=${encodeURIComponent(orderId)}&amount=${amount}`;
}

export default function () {
  const startedAt = Date.now();
  const email = buildEmail();
  const payload = JSON.stringify({
    amount: AMOUNT,
    orderName: ORDER_NAME,
    customerName: randomCustomerName(),
    planType: PLAN_TYPE,
  });

  const createResponse = http.post(`${BASE_URL}/api/payments/toss`, payload, {
    headers: {
      'Content-Type': 'application/json',
      'X-Dev-User-Email': email,
    },
    tags: {
      name: 'create_order',
      flow: 'toss_payment',
    },
    responseCallback: createExpected,
  });

  const createBody = unwrapPayload(parseJson(createResponse));
  const orderId = createBody && createBody.orderId;
  const amount = Number((createBody && createBody.amount) || AMOUNT);
  const successUrl = createBody && createBody.successUrl;

  const orderCreated = check(createResponse, {
    'create order status is 200': (res) => res.status === 200,
    'create orderId exists': () => Boolean(orderId),
    'create amount matches request': () => amount === AMOUNT,
  });

  orderCreateSuccessRate.add(orderCreated);

  if (!orderCreated) {
    paymentFlowDuration.add(Date.now() - startedAt);
    sleep(thinkTime());
    return;
  }

  const paymentKey = `demo-payment-key-${exec.vu.idInTest}-${exec.scenario.iterationInTest}`;
  const confirmUrl = buildConfirmUrl(successUrl, paymentKey, orderId, amount);
  const confirmRequests = Array.from({ length: CONFIRM_BATCH_SIZE }, (_, index) => [
    'GET',
    confirmUrl,
    null,
    {
      headers: {
        'X-Dev-User-Email': email,
      },
      tags: {
        name: 'confirm_payment',
        flow: 'toss_payment',
        confirm_attempt: String(index + 1),
      },
      responseCallback: confirmExpected,
    },
  ]);

  const confirmResponses = http.batch(confirmRequests);

  let successCount = 0;
  let conflictCount = 0;
  let unexpectedCount = 0;

  for (const response of confirmResponses) {
    if (response.status === 200) {
      successCount += 1;
      continue;
    }

    if (response.status === 409) {
      conflictCount += 1;
      continue;
    }

    unexpectedCount += 1;
  }

  confirmSuccessCount.add(successCount);
  confirmConflictCount.add(conflictCount);
  confirmBatchHasSuccessRate.add(successCount >= 1);
  confirmUnexpectedRate.add(unexpectedCount > 0);

  check(confirmResponses, {
    'at least one confirm succeeded': () => successCount >= 1,
    'no unexpected confirm status': () => unexpectedCount === 0,
  });

  paymentFlowDuration.add(Date.now() - startedAt);
  sleep(thinkTime());
}
