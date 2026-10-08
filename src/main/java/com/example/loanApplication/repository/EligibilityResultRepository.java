package com.example.loanApplication.repository;

import com.example.loanApplication.entity.EligibilityResult;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EligibilityResultRepository
        extends JpaRepository<EligibilityResult, Integer> {

    Optional<EligibilityResult>
    findTopByCustomerIdOrderByEligibilityIdDesc(Integer customerId);

}