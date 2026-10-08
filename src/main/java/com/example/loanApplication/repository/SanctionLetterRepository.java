package com.example.loanApplication.repository;

import com.example.loanApplication.entity.SanctionLetter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SanctionLetterRepository
        extends JpaRepository<SanctionLetter, Integer> {

    Optional<SanctionLetter>
    findTopByDealIdOrderBySanctionIdDesc(Integer dealId);

}