package com.dragons.domain.payment;

import com.dragons.domain.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Getter
@Table(name = "bank_deposit")
public class BankDeposit extends BaseEntity {
  private String holder;
  private Long amount;
  @Enumerated(EnumType.STRING)
  private Confirm confirm;

  public static BankDeposit deposit(String holder, Long amount) {
    return new BankDeposit(holder, amount);
  }

  private BankDeposit(String holder, Long amount) {
    this.holder = holder;
    this.amount = amount;
    this.confirm = Confirm.WAITING;
  }

  public void check() {
    this.confirm = Confirm.CONFIRMED;
  }

  public enum Confirm {
    WAITING, CONFIRMED
  }
}
