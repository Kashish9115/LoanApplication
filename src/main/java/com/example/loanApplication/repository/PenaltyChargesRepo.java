package com.example.loanApplication.repository;

import com.example.loanApplication.entity.PenaltyCharges;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PenaltyChargesRepo  extends JpaRepository< PenaltyCharges, Integer> {


    boolean existsByEmiSchedule_EmiScheduleId(Integer emiScheduleId);

    /**
     * 3. Specific EMI schedule ki penalty details fetch karne ke liye (Optional/Utility)
     */
    Optional<PenaltyCharges> findByEmiSchedule_EmiScheduleId(Integer emiScheduleId);

    boolean existsByEmiSchedule_EmiScheduleIdAndReason(Integer emiScheduleId, String reason);


    Page<PenaltyCharges> findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
            Integer loanAccountId,
            Pageable pageable
    );



    Page<PenaltyCharges> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

}
