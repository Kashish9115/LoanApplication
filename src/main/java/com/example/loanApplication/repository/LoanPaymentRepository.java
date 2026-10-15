package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanPayments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanPaymentRepository  extends JpaRepository<LoanPayments,Integer> {
}
