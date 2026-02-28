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

  private Constants() {
  }
}
