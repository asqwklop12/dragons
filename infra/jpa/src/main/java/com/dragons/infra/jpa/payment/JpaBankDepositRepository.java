package com.dragons.infra.jpa.payment;

import com.dragons.domain.payment.BankDeposit;
import com.dragons.domain.payment.BankDeposit.Confirm;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface JpaBankDepositRepository extends JpaRepository<BankDeposit, Long> {
  @Query("""
          SELECT b FROM BankDeposit b WHERE b.confirm =:confirm
         """)
  List<BankDeposit> findAll(Confirm confirm);
}
