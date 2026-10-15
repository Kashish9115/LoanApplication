package com.example.loanApplication.controller;

import com.example.loanApplication.dto.InterestAccrualResponseDTO;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.PenaltyChargesResponseDto;
import com.example.loanApplication.service.PenaltyService;
import com.example.loanApplication.utils.PenaltySchedular;
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

    @GetMapping("/account/{loanAccountId}")
    public ResponseEntity<PageResponse<PenaltyChargesResponseDto>>
    getPenaltiesByAccountId(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                penaltyService.getPenaltiesByAccountId(
                        loanAccountId, page, size)
        );
    }

    private final PenaltySchedular penaltySchedular;

    @PostMapping("/run-overdue-job")
    public String runOverdueJob() {
        penaltySchedular.executeDailyPenaltyAndOverdueJob();
        return "Overdue EMI job executed. Check database and application logs.";
    }






    @GetMapping("/admin/all")
    public ResponseEntity<PageResponse<PenaltyChargesResponseDto>>
    getAllPenaltiesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                penaltyService.getAllPenaltiesForAdmin(page, size)
        );
    }
}
