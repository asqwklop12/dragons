package com.dragons.config.jpa;

import com.dragons.constant.Constants;
import com.dragons.monitoring.app_db_latency.AppDbLatencyMonitor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.jdbc.datasource.DelegatingDataSource;

public class AppDbLatencyDataSource extends DelegatingDataSource {

  private static final String UNKNOWN_TRACE_ID = "N/A";
  private static final String PREPARE_METHODS = "prepareStatement";
  private final AppDbLatencyMonitor monitor;

  public AppDbLatencyDataSource(DataSource targetDataSource, AppDbLatencyMonitor monitor) {
    super(targetDataSource);
    this.monitor = monitor;
  }

  @Override
  @NullMarked
  public Connection getConnection() throws SQLException {
    Connection conn = super.getConnection();
    return (Connection) Proxy.newProxyInstance(
        Connection.class.getClassLoader(),
        new Class[]{Connection.class},
        (proxy, method, args) -> {
          Object result = invoke(conn, method, args);
          if (PREPARE_METHODS.equals(method.getName()) && result instanceof PreparedStatement ps) {
            return wrapPreparedStatement(ps, (String) args[0]);
          }
          return result;
        }
    );
  }

  private PreparedStatement wrapPreparedStatement(PreparedStatement target, String sql) {
    return (PreparedStatement) Proxy.newProxyInstance(
        PreparedStatement.class.getClassLoader(), target.getClass().getInterfaces(),
        (proxy, method, args) -> {
          if (!method.getName().startsWith("execute")) {
            return invoke(target, method, args);
          }
          String traceId = resolveTraceId();
          long startedNanos = System.nanoTime();
          try {
            return invoke(target, method, args);
          } finally {
            monitor.collect(sql, traceId, toElapsedMillis(startedNanos));
          }
        }
    );
  }


  private Object invoke(Object target, Method method, Object[] args) throws Throwable {
    try {
      return method.invoke(target, args);
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  private String resolveTraceId() {
    String traceId = MDC.get(Constants.REQUEST_ID);
    return (traceId == null || traceId.isBlank()) ? UNKNOWN_TRACE_ID : traceId;
  }

  private long toElapsedMillis(long startedNanos) {
    return (System.nanoTime() - startedNanos) / 1_000_000L;
  }
}
