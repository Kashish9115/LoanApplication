package com.example.loanApplication.repository;

import com.example.loanApplication.entity.EmiSchedules;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiSchedulesRepo  extends JpaRepository<Integer, EmiSchedules> {
}
