package com.example.loanApplication.controller;

import com.example.loanApplication.dto.EmiBreakDownDto;
import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.dto.OverdueEmiPaymentBreakdownDto;
import com.example.loanApplication.service.EmiScheduleService;
import com.example.loanApplication.serviceImpl.EmiScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/emi")
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;


private final EmiScheduleService emiService;


@PostMapping("/generate/{loanAccountId}")
public ResponseEntity<List<EmiScheduleResponseDto>> generateInitialSchedule(
        @PathVariable Integer loanAccountId) {

    log.info("REST request to generate initial EMI schedule for LoanAccountId: {}",
            loanAccountId);

    List<EmiScheduleResponseDto> schedules =
            emiService.generateInitialSchedule(loanAccountId);

    return ResponseEntity.ok(schedules);
}


    @GetMapping("/breakdown/{emiScheduleId}")
    public ResponseEntity<EmiBreakDownDto> getEmiBreakdown(
            @PathVariable Integer emiScheduleId) {

        log.info("REST request to fetch EMI breakdown for EmiScheduleId: {}",
                emiScheduleId);

        EmiBreakDownDto breakdown =
                emiService.getEmiBreakdown(emiScheduleId);

        return ResponseEntity.ok(breakdown);
    }

    @PostMapping("/regenerate-schedule/{loanAccountId}")
    public ResponseEntity<List<EmiScheduleResponseDto>> regenerateScheduleOnPartialForeclosure(
            @PathVariable Integer loanAccountId,
            @RequestParam BigDecimal finalSettlementAmount) {

        log.info(
                "REST request to regenerate EMI schedule on partial foreclosure for LoanAccountId: {}, Amount: {}",
                loanAccountId,
                finalSettlementAmount
        );

        List<EmiScheduleResponseDto> updatedSchedules =
                emiService.regenerateScheduleOnPartialForeclosure(
                        loanAccountId,
                        finalSettlementAmount
                );

        return ResponseEntity.ok(updatedSchedules);
    }

    @GetMapping("/loan-account/{loanAccountId}/overdue-breakdown")
    public ResponseEntity<OverdueEmiPaymentBreakdownDto>
    getAllOverdueEmiBreakdown(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                emiScheduleService.getAllOverdueEmiBreakdown(
                        loanAccountId
                )
        );
    }

}