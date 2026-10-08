package com.example.loanApplication.controller;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.service.EmiScheduleService;
import com.example.loanApplication.serviceImpl.EmiScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/emi-schedules")
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;

////    @PostMapping("/generate/{loanAccountId}")
////    public ResponseEntity<List<EmiScheduleResponseDto>> generateEmi(
////            @PathVariable Integer loanAccountId) {
////
////        return ResponseEntity.ok(
////                emiScheduleService.generateEmi(loanAccountId)
////        );
////    }
//
//    @GetMapping("/loan/{loanAccountId}")
//    public ResponseEntity<List<EmiScheduleResponseDto>> getUserEmiSchedules(
//            @PathVariable Integer loanAccountId) {
//
//        return ResponseEntity.ok(
//                emiScheduleService.getUserEmiSchedules(loanAccountId)
//        );
//    }
//
//    @GetMapping("/upcoming/{loanAccountId}")
//    public ResponseEntity<List<EmiScheduleResponseDto>> getUpcomingEmi(
//            @PathVariable Integer loanAccountId) {
//
//        return ResponseEntity.ok(
//                emiScheduleService.getUpcomingEmi(loanAccountId)
//        );
//    }




private final EmiScheduleService emiService;

    /**
     * Fetches the complete multi-month EMI repayment schedule for a customer dashboard.
     */
    @GetMapping("/schedule/{loanAccountId}")
    public ResponseEntity<List<EmiScheduleResponseDto>> getEmiScheduleByAccountId(@PathVariable Integer loanAccountId) {
        List<EmiScheduleResponseDto> schedule = emiService.generateEmiScheduleByLoanAccountId(loanAccountId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Returns an itemized breakdown separating base EMI, bounce fees, and daily accrued interest.
     */
//    @GetMapping("/breakdown/{emiScheduleId}")
//    public ResponseEntity<EmiBreakdownDTO> getItemizedEmiBreakdown(@PathVariable Long emiScheduleId) {
//        EmiBreakdownDTO breakdown = emiService.getItemizedEmiBreakdown(emiScheduleId);
//        return ResponseEntity.ok(breakdown);
//    }

    /**
     * Recalculates and regenerates upcoming unpaid EMI installments after a partial foreclosure.
     */
    @PostMapping("/regenerate/{loanAccountId}")
    public ResponseEntity<List<EmiScheduleResponseDto>> regenerateEmiSchedule(
            @PathVariable Integer loanAccountId,
            @RequestParam BigDecimal newPrincipal) {
        List<EmiScheduleResponseDto> updatedSchedule = emiService.regenerateEmiSchedule(loanAccountId, newPrincipal);
        return ResponseEntity.ok(updatedSchedule);
    }

}