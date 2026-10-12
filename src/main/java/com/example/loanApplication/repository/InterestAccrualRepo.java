package com.example.loanApplication.repository;

import com.example.loanApplication.entity.InterestAccruals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestAccrualRepo extends JpaRepository<InterestAccruals, Integer> {
}
