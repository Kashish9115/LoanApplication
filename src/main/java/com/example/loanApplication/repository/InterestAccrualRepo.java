package com.example.loanApplication.repository;

import com.example.loanApplication.entity.InterestAccruals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterestAccrualRepo extends JpaRepository<InterestAccruals, Integer> {




    boolean existsByLoanAccountIdAndInstallmentNoAndAccrualDateBetween(Integer loanAccountId,
            Integer installmentNo,
            LocalDateTime start,
            LocalDateTime end
    );


}
