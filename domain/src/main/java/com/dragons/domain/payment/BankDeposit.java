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
  private String email;
  private long amount;
  @Enumerated(EnumType.STRING)
  private Confirm confirm;

  public static BankDeposit deposit(String holder, String email, Long amount) {
    return new BankDeposit(holder, email, amount);
  }

  private BankDeposit(String holder, String email, Long amount) {
    this.holder = holder;
    this.email = email;
    this.amount = amount;
    this.confirm = Confirm.WAITING;
  }

  public void check() {
    if (this.confirm != Confirm.WAITING) {
      throw new IllegalStateException("이미 처리된 입금입니다.");
    }
    this.confirm = Confirm.CONFIRMED;
  }

  public enum Confirm {
    WAITING, CONFIRMED
  }
}
