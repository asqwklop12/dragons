package com.dragons.constant;

import java.time.format.DateTimeFormatter;

public final class Constants {

  public static final String PROFILE_PROD = "prod";
  public static final String DEFAULT = "default";

  public static final String EXTRA_REQUEST_ID = "extra_request_id";
  public static final String HEADER_EXTRA_REQUEST_ID = "X_Extra_Request_Id";

  public static final String REQUEST_ID = "request_id";
  public static final String HEADER_REQUEST_ID = "X_Request_Id";


  public static final String TIME_DEFAULT_FORMAT = "yyMMdd HH:mm:ss";

  public static final DateTimeFormatter LEGACY_FORMATTER = DateTimeFormatter.ofPattern(Constants.TIME_DEFAULT_FORMAT);


  public static final class Lock {
    public static final String LOCK_EXPIRED_SUBSCRIPTION = "lock:subscription-expire";
    public static final String LOCK_BANK_DEPOSIT = "lock:bank-deposit";
    public static final String LOCK_PAYMENT_CONFIRM = "lock:payment-confirm:";
    public static final String LOCK_SUBSCRIBE = "lock:subscribe:";


    public static final String LOCK_SCRIPT = """
        if redis.call('get', KEYS[1]) == ARGV[1] then
            return redis.call('del', KEYS[1])
        else
            return 0
        end
        """;
    private Lock() {
    }
  }

  public static final class Metric {

    public static final class DistributedLock {
      public static final String ACQUIRE = "dragons.distributed.lock.acquire";
      public static final String ACQUIRE_TIME = "dragons.distributed.lock.acquire.time";
      public static final String TASK = "dragons.distributed.lock.task";
      public static final String RELEASE = "dragons.distributed.lock.release";

      public static final String TAG_LOCK_TYPE = "lock_type";
      public static final String TAG_LOCK_NAME = "lock_name";
      public static final String TAG_OUTCOME = "outcome";

      public static final String LOCK_TYPE_REDIS = "redis";

      public static final String LOCK_NAME_PAYMENT_CONFIRM = "payment_confirm";
      public static final String LOCK_NAME_SUBSCRIBE = "subscribe";
      public static final String LOCK_NAME_BANK_DEPOSIT = "bank_deposit";
      public static final String LOCK_NAME_EXPIRED_SUBSCRIPTION = "expired_subscription";
      public static final String LOCK_NAME_UNKNOWN = "unknown";

      public static final String OUTCOME_SUCCESS = "success";
      public static final String OUTCOME_FAILURE = "failure";
      public static final String OUTCOME_CONFLICT = "conflict";
      public static final String OUTCOME_FALLBACK = "fallback";

      private DistributedLock() {
      }
    }

    private Metric() {
    }
  }

  private Constants() {
  }
}
