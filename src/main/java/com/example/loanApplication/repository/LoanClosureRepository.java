package com.example.loanApplication.repository;

import com.example.loanApplication.entity.LoanClosure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanClosureRepository  extends JpaRepository<LoanClosure ,Integer> {
}
