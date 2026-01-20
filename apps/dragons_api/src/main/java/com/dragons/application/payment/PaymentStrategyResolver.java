package com.dragons.application.payment;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PaymentStrategyResolver {
  private final Map<PaymentType, PaymentStrategy<?, ?>> strategies;

  public PaymentStrategyResolver(List<PaymentStrategy<?, ?>> strategies) {
    this.strategies = strategies.stream()
        .collect(Collectors.toMap(
            PaymentStrategy::supports,
            Function.identity()
        ));
  }

  public PaymentStrategy<?, ?> confirm() {
    return strategies.get(PaymentType.PG);
  }

  public PaymentStrategy<?, ?> resolve(PaymentType type) {
    return strategies.get(type);
  }
}
