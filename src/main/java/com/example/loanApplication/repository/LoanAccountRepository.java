package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanAccount;

import com.example.loanApplication.enumeration.LoanAccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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


    List<LoanAccount> findByCustomerIdAndStatus(
            Integer customerId,
            LoanAccountStatus status
    );
    Optional<LoanAccount> findByLoanAccountIdAndStatus(
            Integer loanAccountId,
            LoanAccountStatus status
    );
    List<LoanAccount> findByCustomerId(Integer customerId);

    @Query("SELECT l.loanAccountId FROM LoanAccount l WHERE l.customerId = :customerId")
    List<Integer> findLoanAccountIdsByCustomerId(
            @Param("customerId") Integer customerId);}