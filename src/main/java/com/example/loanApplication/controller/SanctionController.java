package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.SanctionResponse;
import com.example.loanApplication.service.SanctionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sanctions")
@RequiredArgsConstructor
public class SanctionController {

    private final SanctionService sanctionService;


    @PostMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<SanctionResponse>> sanctionLoan(
            @PathVariable Integer dealId
    ) {

        ResponseApi<SanctionResponse> response =
                sanctionService.sanctionLoan(dealId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<SanctionResponse>>
    getSanctionByDealId(
            @PathVariable Integer dealId
    ) {

        ResponseApi<SanctionResponse> response =
                sanctionService.getSanctionByDealId(dealId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{sanctionId}")
    public ResponseEntity<ResponseApi<SanctionResponse>>
    getSanctionById(
            @PathVariable Integer sanctionId
    ) {

        ResponseApi<SanctionResponse> response =
                sanctionService.getSanctionById(sanctionId);

        return ResponseEntity.ok(response);
    }
}