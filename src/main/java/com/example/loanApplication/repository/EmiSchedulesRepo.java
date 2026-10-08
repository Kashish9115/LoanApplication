package com.example.loanApplication.repository;

import com.example.loanApplication.entity.EmiSchedules;
import org.springframework.data.jpa.repository.JpaRepository;

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


    boolean existsByLoanAccount_LoanAccountId(
            Integer loanAccountId);

    List<EmiSchedules> findByLoanAccount_LoanAccountId(
            Integer loanAccountId);

    List<EmiSchedules>
    findByLoanAccount_LoanAccountIdAndPaymentStatusAndDueDateGreaterThanEqualOrderByDueDateAsc(
            Integer loanAccountId,
            String paymentStatus,
            LocalDate dueDate);

    List<EmiSchedules>
    findByDueDateLessThanEqualAndPaymentStatus(
            LocalDate dueDate,
            String paymentStatus);

    List<EmiSchedules>
    findByLoanAccount_LoanAccountIdAndPaymentStatus(
            Integer loanAccountId,
            String paymentStatus);


    List<EmiSchedules> findByLoanAccount_LoanAccountIdAndPaymentStatusNotAndDueDateGreaterThanEqualOrderByDueDateAsc(Integer loanAccountId, String paid, LocalDate now);

 List<EmiSchedules> findByLoanAccountIdOrderByInstallmentNoAsc(Integer loanAccountId).

    List<EmiSchedules>
    findByLoanAccount_LoanAccountIdAndPaymentStatusOrderByInstallmentNoAsc(
            Integer loanAccountId,
            String paymentStatus
    );
}
