package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.LoanDealRequest;
import com.example.loanApplication.dto.LoanDealResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.service.LoanDealService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/loan-deals")
@RequiredArgsConstructor
public class LoanDealController {

    private final LoanDealService loanDealService;


    @PostMapping
    public ResponseEntity<ResponseApi<LoanDealResponse>> createLoanDeal(
            @Valid @RequestBody LoanDealRequest request
    ) {

        ResponseApi<LoanDealResponse> response =
                loanDealService.createLoanDeal(request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{dealId}")
    public ResponseEntity<ResponseApi<LoanDealResponse>> getLoanDealById(
            @PathVariable Integer dealId
    ) {

        ResponseApi<LoanDealResponse> response =
                loanDealService.getLoanDealById(dealId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<ResponseApi<LoanDealResponse>>
    getLatestLoanDealByCustomer(
            @PathVariable Integer customerId
    ) {

        ResponseApi<LoanDealResponse> response =
                loanDealService.getLatestLoanDealByCustomer(
                        customerId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ResponseApi<PageResponse<LoanDealResponse>>>
    getLoanDealsByCustomer(
            @PathVariable Integer customerId,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size
    ) {

        ResponseApi<PageResponse<LoanDealResponse>> response =
                loanDealService.getLoanDealsByCustomer(
                        customerId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}