package com.example.loanApplication.repository;

import com.example.loanApplication.entity.ScoreCard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoreCardRepository
        extends JpaRepository<ScoreCard, Integer> {

    Optional<ScoreCard>
    findTopByCustomerIdOrderByScoreCardIdDesc(Integer customerId);
}