package com.dragons.domain.payment;

import java.util.List;

public interface BankDepositRepository {
  List<BankDeposit> findAllWaiting();
}
