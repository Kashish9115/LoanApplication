package com.example.loanApplication.repository;

import com.example.loanApplication.entity.PenaltyCharges;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PenaltyChargesRepo  extends JpaRepository< PenaltyCharges, Integer> {

 //List<PenaltyCharges> findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(Integer loanAccountId);
    /**
     * 1. Loan Account ID ke according saari penalties fetch karta hai (Newest first)
     * Used in PenaltyServiceImpl.getPenaltiesByAccountId()
     */
    List<PenaltyCharges> findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(Integer loanAccountId);

    /**
     * 2. Check karta hai ki specific EMI schedule par bounce penalty pehle se apply hui hai ya nahi
     * Used in PenaltySchedulerJob (Daily Cron Task) to avoid duplicate bounce charges
     */
    boolean existsByEmiSchedule_EmiScheduleId(Integer emiScheduleId);

    /**
     * 3. Specific EMI schedule ki penalty details fetch karne ke liye (Optional/Utility)
     */
    Optional<PenaltyCharges> findByEmiSchedule_EmiScheduleId(Integer emiScheduleId);

}
