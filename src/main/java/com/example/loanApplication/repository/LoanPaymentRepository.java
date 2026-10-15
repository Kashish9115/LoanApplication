package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanPayments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanPaymentRepository extends JpaRepository<LoanPayments, Integer> {

 //List<LoanPayments> findByLoanAccount_LoanAccountIdOrderByPaymentDateDesc(Integer loanAccountId);

    List<LoanPayments> findByLoanAccountIdOrderByPaymentDateDesc(
            Integer loanAccountId
    );

    // Fetch last 5 payment transactions for active loan
    List<LoanPayments> findTop5ByLoanAccountIdOrderByPaymentDateDesc(Integer loanAccountId);

}
