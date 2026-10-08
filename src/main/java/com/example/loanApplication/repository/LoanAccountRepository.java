package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanAccount;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanAccountRepository
        extends JpaRepository<LoanAccount, Integer> {

    Optional<LoanAccount> findByDealId(
            Integer dealId
    );

    Optional<LoanAccount>
    findTopByCustomerIdOrderByLoanAccountIdDesc(
            Integer customerId
    );






    Page<LoanAccount>
    findByCustomerIdOrderByLoanAccountIdDesc(
            Integer customerId,
            Pageable pageable
    );
}