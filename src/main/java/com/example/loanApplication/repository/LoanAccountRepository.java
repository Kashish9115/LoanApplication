package com.example.loanApplication.repository;

import com.example.loanApplication.dto.dashboard.PortfolioBreakdownDto;
import com.example.loanApplication.entity.LoanAccount;
import com.example.loanApplication.enumeration.LoanAccountStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // All customers' loan accounts, newest first.
    Page<LoanAccount> findAllByOrderByLoanAccountIdDesc(
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


// Dashboard methods

    // Count loans by status (ACTIVE, CLOSED)
    Long countByStatus(LoanAccountStatus status);

    // Total Portfolio Value for ACTIVE loans
    @Query("""
        SELECT COALESCE(SUM(la.outstandingAmount), 0)
        FROM LoanAccount la
        WHERE la.status = :status
        """)
    BigDecimal calculateTotalPortfolioValue(
            @org.springframework.data.repository.query.Param("status")
            LoanAccountStatus status
    );

    // Product-wise portfolio breakdown
    @Query("""
        SELECT new com.example.loanApplication.dto.dashboard.PortfolioBreakdownDto(
            ld.loanType,
            SUM(la.outstandingAmount),
            COUNT(la)
        )
        FROM LoanAccount la
        JOIN LoanDeal ld ON la.dealId = ld.dealId
        WHERE la.status = :status
        GROUP BY ld.loanType
        """)
    List<PortfolioBreakdownDto> fetchPortfolioBreakdown(
            @org.springframework.data.repository.query.Param("status")
            LoanAccountStatus status
    );

    // Date range filtering on LoanAccount.createdAt
    List<LoanAccount> findByCreatedAtBetween(
            LocalDateTime fromDate,
            LocalDateTime toDate
    );


    @Query("SELECT l.loanAccountId FROM LoanAccount l WHERE l.customerId = :customerId")
    List<Integer> findLoanAccountIdsByCustomerId(
            @Param("customerId") Integer customerId);
}