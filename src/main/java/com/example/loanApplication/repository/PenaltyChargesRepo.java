package com.example.loanApplication.repository;

import com.example.loanApplication.entity.PenaltyCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenaltyChargesRepo  extends JpaRepository< PenaltyCharges, Integer> {

 List<PenaltyCharges> findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(Integer loanAccountId);


}
