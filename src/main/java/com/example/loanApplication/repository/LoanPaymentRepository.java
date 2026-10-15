package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanPayments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;



public interface LoanPaymentRepository extends JpaRepository<LoanPayments, Integer> {


    Page<LoanPayments> findByLoanAccountIdOrderByPaymentDateDesc(
            Integer loanAccountId, Pageable pageable
    );

}
