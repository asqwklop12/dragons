package com.dragons.application.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dragons.domain.lock.DistributedLockExecutor;
import com.dragons.domain.lock.DistributedLockFactory;
import com.dragons.domain.lock.LockException;
import com.dragons.domain.lock.LockOptions;
import com.dragons.domain.lock.LockType;
import com.dragons.domain.payment.Payment;
import com.dragons.domain.payment.PaymentRepository;
import com.dragons.domain.payment.PgPaymentClient;
import com.dragons.domain.payment.TossPaymentConfirmation;
import com.dragons.domain.subscription.Subscription;
import com.dragons.domain.subscription.SubscriptionRepository;
import com.dragons.support.error.CoreException;
import com.dragons.support.error.ErrorType;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class PgPaymentStrategyTest {

  @Mock
  private Clock clock;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private DistributedLockFactory distributedLockFactory;

  @Mock
  private DistributedLockExecutor distributedLockExecutor;

  @Mock
  private PgPaymentClient pgPaymentClient;

  private PgPaymentStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new PgPaymentStrategy(clock, subscriptionRepository, paymentRepository, distributedLockFactory,
        pgPaymentClient);
    lenient().when(clock.instant()).thenReturn(Instant.parse("2026-03-20T00:00:00Z"));
    lenient().when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
  }

  @Test
  @DisplayName("이미 같은 paymentKey로 처리된 confirm 재요청은 성공으로 종료한다")
  void success_idempotent_whenAlreadyConfirmedWithSamePaymentKey() {
    Payment payment = Payment.createOrder("order-1", "홍길동", "test@example.com", 9900L, "premium", "TOSS");
    payment.updateKey("payment-key-1");

    when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));

    strategy.success("payment-key-1", "order-1", 9900L);

    verify(distributedLockExecutor, never()).executeWithLock(any(), any(), any());
    verify(pgPaymentClient, never()).confirm(any(), any(), anyLong());
    verify(subscriptionRepository, never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("이미 다른 paymentKey로 처리된 confirm 재요청은 conflict를 반환한다")
  void success_conflict_whenAlreadyConfirmedWithDifferentPaymentKey() {
    Payment payment = Payment.createOrder("order-1", "홍길동", "test@example.com", 9900L, "premium", "TOSS");
    payment.updateKey("payment-key-1");

    when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));

    assertThatThrownBy(() -> strategy.success("payment-key-2", "order-1", 9900L))
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
          CoreException coreException = (CoreException) exception;
          assertThat(coreException.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        });

    verify(distributedLockExecutor, never()).executeWithLock(any(), any(), any());
    verify(pgPaymentClient, never()).confirm(any(), any(), anyLong());
  }

  @Test
  @DisplayName("락을 획득하지 못한 동시 confirm 요청은 conflict를 반환한다")
  void success_conflict_whenLockNotAcquired() {
    Payment payment = Payment.createOrder("order-1", "홍길동", "test@example.com", 9900L, "premium", "TOSS");

    when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));
    when(distributedLockFactory.get(LockType.REDIS)).thenReturn(distributedLockExecutor);
    when(distributedLockExecutor.executeWithLock(eq("lock:payment-confirm:order-1"),
        eq(LockOptions.of(Duration.ofSeconds(10))), any()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> strategy.success("payment-key-1", "order-1", 9900L))
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
          CoreException coreException = (CoreException) exception;
          assertThat(coreException.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        });

    verify(pgPaymentClient, never()).confirm(any(), any(), anyLong());
    verify(subscriptionRepository, never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("분산락 처리 오류는 lock 관련 5XX로 구분된다")
  void success_lockError_whenLockExecutorFails() {
    Payment payment = Payment.createOrder("order-1", "홍길동", "test@example.com", 9900L, "premium", "TOSS");

    when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));
    when(distributedLockFactory.get(LockType.REDIS)).thenReturn(distributedLockExecutor);
    when(distributedLockExecutor.executeWithLock(eq("lock:payment-confirm:order-1"),
        eq(LockOptions.of(Duration.ofSeconds(10))), any()))
        .thenThrow(new LockException("redis unavailable"));

    assertThatThrownBy(() -> strategy.success("payment-key-1", "order-1", 9900L))
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
          CoreException coreException = (CoreException) exception;
          assertThat(coreException.getErrorType()).isEqualTo(ErrorType.LOCK_ERROR);
        });

    verify(pgPaymentClient, never()).confirm(any(), any(), anyLong());
    verify(subscriptionRepository, never()).save(any(Subscription.class));
  }

  @Test
  @DisplayName("최초 confirm 성공 시 paymentKey 갱신과 구독 처리가 수행된다")
  void success_updatesPaymentAndSubscriptionOnFirstConfirm() {
    Payment payment = Payment.createOrder("order-1", "홍길동", "test@example.com", 9900L, "premium", "TOSS");

    when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));
    when(distributedLockFactory.get(LockType.REDIS)).thenReturn(distributedLockExecutor);
    when(distributedLockExecutor.executeWithLock(any(), any(), any()))
        .thenAnswer(invocation -> {
          @SuppressWarnings("unchecked")
          Supplier<Object> supplier = invocation.getArgument(2, Supplier.class);
          return Optional.ofNullable(supplier.get());
        });
    when(distributedLockExecutor.runWithLock(any(), any(), any()))
        .thenAnswer(invocation -> {
          Runnable runnable = invocation.getArgument(2, Runnable.class);
          runnable.run();
          return true;
        });
    when(subscriptionRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(subscriptionRepository.findExpiredByEmail("test@example.com")).thenReturn(Optional.empty());
    when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(pgPaymentClient.confirm("payment-key-1", "order-1", 9900L))
        .thenReturn(new TossPaymentConfirmation("payment-key-1", "order-1", 9900L));

    strategy.success("payment-key-1", "order-1", 9900L);

    assertThat(payment.paymentKey()).isEqualTo("payment-key-1");
    verify(pgPaymentClient).confirm("payment-key-1", "order-1", 9900L);
    verify(subscriptionRepository).save(any(Subscription.class));
  }

  @Test
  @DisplayName("구독 이메일 저장 충돌은 conflict를 반환한다")
  void success_conflict_whenSubscriptionPersistenceCollides() {
    Payment payment = Payment.createOrder("order-1", "홍길동", "test@example.com", 9900L, "premium", "TOSS");

    when(paymentRepository.findByOrderId("order-1")).thenReturn(Optional.of(payment));
    when(distributedLockFactory.get(LockType.REDIS)).thenReturn(distributedLockExecutor);
    when(distributedLockExecutor.executeWithLock(any(), any(), any()))
        .thenAnswer(invocation -> {
          @SuppressWarnings("unchecked")
          Supplier<Object> supplier = invocation.getArgument(2, Supplier.class);
          return Optional.ofNullable(supplier.get());
        });
    when(distributedLockExecutor.runWithLock(any(), any(), any()))
        .thenAnswer(invocation -> {
          Runnable runnable = invocation.getArgument(2, Runnable.class);
          runnable.run();
          return true;
        });
    when(pgPaymentClient.confirm("payment-key-1", "order-1", 9900L))
        .thenReturn(new TossPaymentConfirmation("payment-key-1", "order-1", 9900L));
    when(subscriptionRepository.existsByEmail("test@example.com")).thenReturn(false);
    when(subscriptionRepository.findExpiredByEmail("test@example.com")).thenReturn(Optional.empty());
    when(subscriptionRepository.save(any(Subscription.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate subscription email"));

    assertThatThrownBy(() -> strategy.success("payment-key-1", "order-1", 9900L))
        .isInstanceOf(CoreException.class)
        .satisfies(exception -> {
          CoreException coreException = (CoreException) exception;
          assertThat(coreException.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        });
  }
}
