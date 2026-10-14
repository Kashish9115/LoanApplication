package com.example.loanApplication.repository;

import com.example.loanApplication.entity.ForeClosureRequest;
import com.example.loanApplication.enumeration.ForeClosureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForeClosureRequestRepository
        extends JpaRepository<ForeClosureRequest, Integer> {

    List<ForeClosureRequest> findByLoanAccountIdIn(
            List<Integer> loanAccountIds
    );
    List<ForeClosureRequest> findByStatus(String status);

    Optional<ForeClosureRequest> findByRequestIdAndStatus(
            Integer requestId,
            String status
    );

}