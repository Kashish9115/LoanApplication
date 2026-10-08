package com.example.loanApplication.controller;

import com.example.loanApplication.dto.EmiScheduleResponseDto;
import com.example.loanApplication.service.EmiScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emi-schedules")
@RequiredArgsConstructor
public class EmiScheduleController {

    private final EmiScheduleService emiScheduleService;

//    @PostMapping("/generate/{loanAccountId}")
//    public ResponseEntity<List<EmiScheduleResponseDto>> generateEmi(
//            @PathVariable Integer loanAccountId) {
//
//        return ResponseEntity.ok(
//                emiScheduleService.generateEmi(loanAccountId)
//        );
//    }

    @GetMapping("/loan/{loanAccountId}")
    public ResponseEntity<List<EmiScheduleResponseDto>> getUserEmiSchedules(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                emiScheduleService.getUserEmiSchedules(loanAccountId)
        );
    }

    @GetMapping("/upcoming/{loanAccountId}")
    public ResponseEntity<List<EmiScheduleResponseDto>> getUpcomingEmi(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                emiScheduleService.getUpcomingEmi(loanAccountId)
        );
    }
}