package com.example.loanApplication.repository;

import com.example.loanApplication.dto.EmiReminderDto;
import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.entity.EmiSchedules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmiSchedulesRepo  extends JpaRepository< EmiSchedules, Integer> {

//    List<EmiSchedules> findByLoanAccountIdOrderByInstallmentNoAsc(
//            Integer loanAccountId);
//
//    Optional<EmiSchedules> findByEmiScheduleId(
//            Integer emiScheduleId);
//
//    List<EmiSchedules> findByPaymentStatusAndDueDateLessThanEqual(
//            String paymentStatus,
//            LocalDateTime dueDate);
//
//    List<EmiSchedules> findByLoanAccountIdAndPaymentStatus(
//            Integer loanAccountId,
//            String paymentStatus);


//    boolean existsByLoanAccount_LoanAccountId(
//            Integer loanAccountId);
//
//    List<EmiSchedules> findByLoanAccount_LoanAccountId(
//            Integer loanAccountId);
//
//    List<EmiSchedules>
//    findByLoanAccount_LoanAccountIdAndPaymentStatusAndDueDateGreaterThanEqualOrderByDueDateAsc(
//            Integer loanAccountId,
//            String paymentStatus,
//            LocalDate dueDate);
//
//    List<EmiSchedules>
//    findByDueDateLessThanEqualAndPaymentStatus(
//            LocalDate dueDate,
//            String paymentStatus);
//
//    List<EmiSchedules>
//    findByLoanAccount_LoanAccountIdAndPaymentStatus(
//            Integer loanAccountId,
//            String paymentStatus);
//
//
    List<EmiSchedules> findByLoanAccount_LoanAccountIdAndPaymentStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(Integer loanAccountId, String paid, LocalDate now);

 //List<EmiSchedules> findByLoanAccountIdOrderByInstallmentNoAsc(Integer loanAccountId);

    List<EmiSchedules>
    findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
            Integer loanAccountId,
            String paymentStatus
    );



    /**
     * 3. Loan Account ID ke base par saare EMI schedules installment number wise fetch karne ke liye
     * Used in: Customer/Officer Dashboard EMI Schedule View
     */
    List<EmiSchedules> findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId
    );

  //     List<EmiScheduleResponseDto> findByDueDateAndPaymentStatus(LocalDate targetDate, String status);




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



    /**
     * 2. Daily Scheduler Job ke liye: Aaj ki date se pehle ki sabhi PENDING overdue EMIs fetch karta hai
     * Used in: PenaltySchedulerJob (Raat 12 baje auto-run hone waale cron job mein)
     */
    List<EmiSchedules> findByDueDateBeforeAndPaymentStatus(LocalDate dueDate, String paymentStatus);




 }