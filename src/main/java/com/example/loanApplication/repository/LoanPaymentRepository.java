package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanPayments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayments, Integer> {

 //List<LoanPayments> findByLoanAccount_LoanAccountIdOrderByPaymentDateDesc(Integer loanAccountId);

    Page<LoanPayments> findByLoanAccountIdOrderByPaymentDateDesc(
            Integer loanAccountId, Pageable pageable
    );

}
