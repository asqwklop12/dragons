package com.dragons.config.jpa;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;


@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

  @Override
  protected @Nullable Object determineCurrentLookupKey() {
    boolean readOnly =
        TransactionSynchronizationManager.isCurrentTransactionReadOnly();
    String key = readOnly
        ? "REPLICA"   // 랜덤 or round robin
        : "PRIMARY";

    log.info("### Selected DB: {}, key: {}", readOnly, key);

    return key;
  }
}
