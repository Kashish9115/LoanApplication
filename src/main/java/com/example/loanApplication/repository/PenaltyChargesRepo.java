package com.example.loanApplication.repository;

import com.example.loanApplication.entity.PenaltyCharges;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyChargesRepo  extends JpaRepository<Integer, PenaltyCharges> {
}
