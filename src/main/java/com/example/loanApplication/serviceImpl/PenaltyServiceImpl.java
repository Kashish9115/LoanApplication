package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;
import com.example.loanApplication.entity.EmiSchedules;
import com.example.loanApplication.entity.InterestAccruals;
import com.example.loanApplication.entity.PenaltyCharges;
import com.example.loanApplication.enumeration.AccrualStatus;
import com.example.loanApplication.exception.EmiScheduleNotFound;
import com.example.loanApplication.repository.InterestAccrualRepo;
import com.example.loanApplication.repository.PenaltyChargesRepo;
import com.example.loanApplication.service.NotificationService;
import com.example.loanApplication.service.PenaltyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PenaltyServiceImpl  implements PenaltyService {
   private  final PenaltyChargesRepo penaltyChargesRepo;
   private  final ModelMapper modelMapper;
   private  final InterestAccrualRepo interestAccrualRepo;
private  final NotificationService notificationService;



    private static final BigDecimal BOUNCE_CHARGE =
            new BigDecimal("600.00");

    private static final BigDecimal DAILY_LATE_FEE =
            new BigDecimal("1.00");

    private static final String BOUNCE_REASON =
            "Bounce Charge - Overdue EMI";


    @Override
    public PageResponse<PenaltyChargesResponseDto> getPenaltiesByAccountId(
            Integer loanAccountId, int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<PenaltyCharges> penalties =
                penaltyChargesRepo
                        .findByLoanAccount_LoanAccountIdOrderByCreatedAtDesc(
                                loanAccountId, pageable);

        List<PenaltyChargesResponseDto> responseDto =
                penalties.getContent()
                        .stream()
                        .map(p -> modelMapper.map(
                                p, PenaltyChargesResponseDto.class))
                        .toList();

        return PageResponse.<PenaltyChargesResponseDto>builder()
                .content(responseDto)
                .pageNumber(penalties.getNumber())
                .pageSize(penalties.getSize())
                .totalElements(penalties.getTotalElements())
                .totalPages(penalties.getTotalPages())
                .first(penalties.isFirst())
                .last(penalties.isLast())
                .build();
    }



    @Override
    public PageResponse<PenaltyChargesResponseDto> getAllPenaltiesForAdmin(
            int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<PenaltyCharges> penalties =
                penaltyChargesRepo.findAllByOrderByCreatedAtDesc(pageable);

        List<PenaltyChargesResponseDto> responseDto =
                penalties.getContent()
                        .stream()
                        .map(p -> modelMapper.map(
                                p, PenaltyChargesResponseDto.class))
                        .toList();

        return PageResponse.<PenaltyChargesResponseDto>builder()
                .content(responseDto)
                .pageNumber(penalties.getNumber())
                .pageSize(penalties.getSize())
                .totalElements(penalties.getTotalElements())
                .totalPages(penalties.getTotalPages())
                .first(penalties.isFirst())
                .last(penalties.isLast())
                .build();
    }


    @Override
    @Transactional
    public void applyBounceCharge(EmiSchedules schedule) {

        Integer scheduleId = schedule.getEmiScheduleId();

        boolean alreadyApplied =
                penaltyChargesRepo
                        .existsByEmiSchedule_EmiScheduleIdAndReason(
                                scheduleId,
                                "Bounce Charge - Overdue EMI"
                        );

        if (alreadyApplied) {
            return;
        }

        PenaltyCharges penalty = new PenaltyCharges();
        penalty.setEmiSchedule(schedule);
        penalty.setLoanAccount(schedule.getLoanAccount());
        penalty.setPenaltyAmount(new BigDecimal("600.00"));
        penalty.setReason("Bounce Charge - Overdue EMI");
        penalty.setStatus("APPLIED");
        penalty.setCreatedAt(LocalDateTime.now());

        penaltyChargesRepo.save(penalty);
    }





    @Override
    @Transactional
    public void processOverdueEmi(
            EmiSchedules schedule, LocalDate today) {

        if (schedule == null
                || schedule.getDueDate() == null
                || schedule.getLoanAccount() == null
                || schedule.getInstallmentNo() == null) {

            throw new EmiScheduleNotFound(
                    "Invalid EMI schedule for overdue processing"
            );
        }


        if (!"PENDING".equalsIgnoreCase(schedule.getPaymentStatus())
                || !schedule.getDueDate().isBefore(today)) {
            return;
        }
        applyBounceCharge(schedule);
        Integer loanAccountId =
                schedule.getLoanAccount().getLoanAccountId();

        Integer installmentNo = schedule.getInstallmentNo();

        LocalDate accrualDate = schedule.getDueDate().plusDays(1);
        boolean todayAccrualCreated = false;

        while (!accrualDate.isAfter(today)) {

            LocalDateTime start = accrualDate.atStartOfDay();
            LocalDateTime end = accrualDate.plusDays(1)
                    .atStartOfDay()
                    .minusNanos(1);

            boolean exists =
                    interestAccrualRepo
                            .existsByLoanAccountIdAndInstallmentNoAndAccrualDateBetween(
                                    loanAccountId,
                                    installmentNo,
                                    start,
                                    end
                            );

            if (!exists) {
                InterestAccruals accrual = new InterestAccruals();

                accrual.setLoanAccountId(loanAccountId);
                accrual.setInstallmentNo(installmentNo);
                accrual.setInterestAmount(new BigDecimal("1.00"));
                accrual.setAccrualDate(accrualDate.atStartOfDay());
                accrual.setStatus(AccrualStatus.APPLIED.name());

                interestAccrualRepo.save(accrual);

                if (accrualDate.equals(today)) {
                    todayAccrualCreated = true;
                }
            }

            accrualDate = accrualDate.plusDays(1);
        }

        // Send notification only if today's fee was newly recorded
        if (todayAccrualCreated) {

            Integer customerId =
                    schedule.getLoanAccount().getCustomerId();

            if (customerId != null) {

                long daysOverdue = ChronoUnit.DAYS.between(
                        schedule.getDueDate(), today
                );

                BigDecimal totalLateFee =
                        BigDecimal.valueOf(daysOverdue);

                notificationService.sendNotification(
                        customerId,
                        "Your EMI installment #"+ installmentNo + " is overdue by " + daysOverdue + " days. Accumulated late fee: ₹" + totalLateFee + ". Please pay your overdue EMI.",
                        "Overdue EMI Payment Alert"
                );
            }
        }
    }
}
