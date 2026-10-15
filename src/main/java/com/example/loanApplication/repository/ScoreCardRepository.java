package com.example.loanApplication.repository;

import com.example.loanApplication.entity.ScoreCard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreCardRepository
        extends JpaRepository<ScoreCard, Integer> {

    Optional<ScoreCard>
    findTopByCustomerIdOrderByScoreCardIdDesc(Integer customerId);



    //Dashboard methods -

    // Count applications by current status (APPROVED, REJECTED, PENDING)
    Long countByCurrentStatus(String currentStatus);

    // Monthly Application Trends across overall data
  /*  @Query("""
            SELECT MONTH(s.appliedDate), COUNT(s)
            FROM ScoreCard s
            GROUP BY MONTH(s.appliedDate)
            ORDER BY MONTH(s.appliedDate)
            """)
    List<Object[]> fetchMonthlyApplicationTrends();*/

    // Dashboard methods

    // Monthly Application Trends
    @Query("""
        SELECT MONTH(s.appliedDate), COUNT(s)
        FROM ScoreCard s
        WHERE s.appliedDate IS NOT NULL
        GROUP BY MONTH(s.appliedDate)
        ORDER BY MONTH(s.appliedDate)
        """)
    List<Object[]> fetchMonthlyApplicationTrends();


    //report methods

// 1. When dates are NOT provided (All Data)

    Long countByCibilScoreBetween(
            Integer minScore,
            Integer maxScore
    );

    Long countByCibilScoreBetweenAndCurrentStatus(
            Integer minScore,
            Integer maxScore,
            String currentStatus
    );

// 2. When dates ARE provided (Filtered Data)

    Long countByCibilScoreBetweenAndAppliedDateBetween(
            Integer minScore,
            Integer maxScore,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    Long countByCibilScoreBetweenAndCurrentStatusAndAppliedDateBetween(
            Integer minScore,
            Integer maxScore,
            String currentStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    // --- Score Card / Risk Category Queries (Without Date Filter) ---
    Long countByRiskCategoryContainingIgnoreCase(String riskCategory);

    Long countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCase(
            String riskCategory,
            String currentStatus
    );

    // --- Score Card / Risk Category Queries (With Date Filter) ---
    Long countByRiskCategoryContainingIgnoreCaseAndAppliedDateBetween(
            String riskCategory,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

    Long countByRiskCategoryContainingIgnoreCaseAndCurrentStatusIgnoreCaseAndAppliedDateBetween(
            String riskCategory,
            String currentStatus,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );



}