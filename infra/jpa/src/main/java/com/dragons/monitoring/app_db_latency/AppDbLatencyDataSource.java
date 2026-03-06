package com.dragons.monitoring.app_db_latency;

import com.dragons.constant.Constants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.MDC;
import org.springframework.jdbc.datasource.DelegatingDataSource;

public class AppDbLatencyDataSource extends DelegatingDataSource {

  private static final String UNKNOWN_TRACE_ID = "N/A";
  private final AppDbLatencyMonitor monitor;

  public AppDbLatencyDataSource(DataSource targetDataSource, AppDbLatencyMonitor monitor) {
    super(targetDataSource);
    this.monitor = monitor;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return wrapConnection(super.getConnection());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return wrapConnection(super.getConnection(username, password));
  }

  private Connection wrapConnection(Connection target) {
    return (Connection) Proxy.newProxyInstance(
        Connection.class.getClassLoader(),
        new Class[]{Connection.class},
        (proxy, method, args) -> {
          Object result = invoke(target, method, args);
          String methodName = method.getName();

          if (!"prepareStatement".equals(methodName) && !"prepareCall".equals(methodName)) {
            return result;
          }

          if (!(result instanceof PreparedStatement preparedStatement)) {
            return result;
          }

          String sql = (String) args[0];
          return wrapPreparedStatement(preparedStatement, sql);
        }
    );
  }

  private PreparedStatement wrapPreparedStatement(PreparedStatement target, String sql) {
    return (PreparedStatement) Proxy.newProxyInstance(
        PreparedStatement.class.getClassLoader(),
        statementInterfaces(target),
        (proxy, method, args) -> {
          if (!method.getName().startsWith("execute")) {
            return invoke(target, method, args);
          }

          String traceId = resolveTraceId();
          long startedNanos = System.nanoTime();

          try {
            Object result = invoke(target, method, args);
            monitor.collect(sql, traceId, toElapsedMillis(startedNanos));
            return result;
          } catch (Throwable throwable) {
            monitor.collect(sql, traceId, toElapsedMillis(startedNanos));
            throw throwable;
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



  private Class<?>[] statementInterfaces(PreparedStatement target) {
    if (target instanceof CallableStatement) {
      return new Class[]{CallableStatement.class, PreparedStatement.class};
    }
    return new Class[]{PreparedStatement.class};
  }

  private String resolveTraceId() {
    String traceId = MDC.get(Constants.REQUEST_ID);
    if (traceId == null || traceId.isBlank()) {
      return UNKNOWN_TRACE_ID;
    }
    return traceId;
  }

  private long toElapsedMillis(long startedNanos) {
    return (System.nanoTime() - startedNanos) / 1_000_000L;
  }
}
