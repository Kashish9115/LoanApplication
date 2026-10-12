package com.example.loanApplication.controller;

import com.example.loanApplication.dto.InterestAccrualResponseDTO;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;
import com.example.loanApplication.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/penalties")
@RequiredArgsConstructor
public class PenaltyController {



    private final PenaltyService penaltyService;

    @PostMapping("/add-bounce")
    public ResponseEntity<PenaltyChargesResponseDto> applyBounceCharge(
            @RequestParam Integer emiScheduleId,
            @RequestParam(required = false) BigDecimal bounceAmount) {
        return ResponseEntity.ok(penaltyService.applyBounceCharge(emiScheduleId, bounceAmount));
    }

    @PostMapping("/calculate-late-interest")
    public ResponseEntity<InterestAccrualResponseDTO> calculateLateInterest(
            @RequestParam Integer loanAccountId,
            @RequestParam int daysOverdue) {
        return ResponseEntity.ok(penaltyService.calculateDailyLateInterest(loanAccountId, daysOverdue));
    }

//    @PostMapping("/waive")
//    public ResponseEntity<PenaltyChargesResponseDto> waivePenalty(
//            @RequestParam Long penaltyChargeId,
//            @RequestParam Long ticketId) {
//        return ResponseEntity.ok(penaltyService.waivePenaltyCharge(penaltyChargeId, ticketId));
//    }

    @GetMapping("/history/{loanAccountId}")
    public ResponseEntity<List<PenaltyChargesResponseDto>> getPenalties(@PathVariable Integer loanAccountId) {
        return ResponseEntity.ok(penaltyService.getPenaltiesByAccountId(loanAccountId));
    }
}
