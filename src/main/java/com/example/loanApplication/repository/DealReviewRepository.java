package com.example.loanApplication.repository;

import com.example.loanApplication.entity.DealReview;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DealReviewRepository
        extends JpaRepository<DealReview, Integer> {

    Optional<DealReview>
    findTopByDealIdOrderByReviewIdDesc(
            Integer dealId
    );

    Page<DealReview>
    findByOfficerIdOrderByReviewIdDesc(
            Integer officerId,
            Pageable pageable
    );

    Page<DealReview>
    findByDealIdOrderByReviewIdDesc(
            Integer dealId,
            Pageable pageable
    );
}