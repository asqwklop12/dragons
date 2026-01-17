package com.dragons.infra.jpa.payment;

import com.dragons.domain.payment.BankDeposit;
import com.dragons.domain.payment.BankDeposit.Confirm;
import com.dragons.domain.payment.BankDepositRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class BankDepositRepositoryImpl implements BankDepositRepository {
  private final JpaBankDepositRepository jpaBankDepositRepository;


  @Override
  public List<BankDeposit> findAll() {
   return jpaBankDepositRepository.findAll(Confirm.WAITING);
  }
}
