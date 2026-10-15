package com.example.loanApplication.repository;

import com.example.loanApplication.dto.EmiReminderDto;
import com.example.loanApplication.entity.EmiSchedules;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Lock;
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

    List<EmiSchedules> findByLoanAccount_LoanAccountIdOrderByInstallmentNoAsc(
            Integer loanAccountId
    );


    List<EmiSchedules> findByDueDateBeforeAndPaymentStatus(LocalDate dueDate, String paymentStatus);    @Query("""
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


    //dashboard methods
    // Overall Monthly Collection Efficiency
    @Query("""
            SELECT MONTH(e.dueDate),
                   SUM(e.emi),
                   SUM(
                       CASE
                           WHEN e.paymentStatus = 'PAID'
                           THEN e.emi
                           ELSE 0
                       END
                   )
            FROM EmiSchedules e
            GROUP BY MONTH(e.dueDate)
            ORDER BY MONTH(e.dueDate)
            """)
    List<Object[]> fetchCollectionEfficiencyByMonth();


    // Count paid EMIs
    Long countByLoanAccount_LoanAccountIdAndPaymentStatus(Integer loanAccountId, String paymentStatus);

    // Fetch upcoming pending EMI sorted by due date
    Optional<EmiSchedules> findFirstByLoanAccount_LoanAccountIdAndPaymentStatusOrderByDueDateAsc(
            Integer loanAccountId, String paymentStatus
    );









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
