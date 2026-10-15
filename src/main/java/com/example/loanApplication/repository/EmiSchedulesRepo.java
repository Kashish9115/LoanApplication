package com.example.loanApplication.repository;

import com.example.loanApplication.dto.EmiReminderDto;
import com.example.loanApplication.entity.EmiSchedules;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

public interface EmiSchedulesRepo  extends JpaRepository< EmiSchedules, Integer> {



    List<EmiSchedules>
    findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
            Integer loanAccountId,
            String paymentStatus
    );




    List<EmiSchedules> findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId
    );





    @Query("""
    SELECT new com.example.loanApplication.dto.EmiReminderDto(
        e.loanAccount.customerId,
        e.installmentNo,
        e.dueDate,
        e.emi,
        e.paymentStatus
    )
    FROM EmiSchedules e
    WHERE e.dueDate = :dueDate
      AND e.paymentStatus = :status
""")
    List<EmiReminderDto> findEmiReminders(
            @Param("dueDate") LocalDate dueDate,
            @Param("status") String status
    );




    List<EmiSchedules> findByDueDateBeforeAndPaymentStatus(LocalDate dueDate, String paymentStatus);



    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
       SELECT e FROM EmiSchedules e
       WHERE e.emiScheduleId = :emiScheduleId
       """)
    Optional<EmiSchedules> findByIdForUpdate(
            @Param("emiScheduleId") Integer emiScheduleId
    );



    Page<EmiSchedules>
    findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(Integer loanAccountId, Pageable pageable
    );

}