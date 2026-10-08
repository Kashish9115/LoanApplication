package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.LoanAccountResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.service.LoanAccountService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan-accounts")
@RequiredArgsConstructor
public class LoanAccountController {

    private final LoanAccountService loanAccountService;


    @PostMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    createLoanAccount(
            @PathVariable Integer dealId
    ) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.createLoanAccount(
                        dealId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{loanAccountId}")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    getLoanAccountById(
            @PathVariable Integer loanAccountId
    ) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.getLoanAccountById(
                        loanAccountId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    getLoanAccountByDealId(
            @PathVariable Integer dealId
    ) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.getLoanAccountByDealId(
                        dealId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    getLatestLoanAccountByCustomerId(
            @PathVariable Integer customerId
    ) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService
                        .getLatestLoanAccountByCustomerId(
                                customerId
                        );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ResponseApi<PageResponse<LoanAccountResponse>>>
    getLoanAccountsByCustomerId(
            @PathVariable Integer customerId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        ResponseApi<PageResponse<LoanAccountResponse>> response =
                loanAccountService
                        .getLoanAccountsByCustomerId(
                                customerId,
                                page,
                                size
                        );

        return ResponseEntity.ok(response);
    }
}