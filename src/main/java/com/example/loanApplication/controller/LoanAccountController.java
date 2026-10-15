
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

    /*
     * GET ALL LOAN ACCOUNTS
     *
     * Example:
     * GET /api/v1/loan-accounts?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ResponseApi<PageResponse<LoanAccountResponse>>>
    getAllLoanAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResponseApi<PageResponse<LoanAccountResponse>> response =
                loanAccountService.getAllLoanAccounts(page, size);

        return ResponseEntity.ok(response);
    }

    /*
     * CREATE LOAN ACCOUNT
     *
     * Example:
     * POST /api/v1/loan-accounts/deal/8
     */
    @PostMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    createLoanAccount(
            @PathVariable Integer dealId) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.createLoanAccount(dealId);

        return ResponseEntity.ok(response);
    }

    /*
     * GET LOAN ACCOUNT BY ID
     *
     * Example:
     * GET /api/v1/loan-accounts/8
     */
    @GetMapping("/{loanAccountId}")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    getLoanAccountById(
            @PathVariable Integer loanAccountId) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.getLoanAccountById(loanAccountId);

        return ResponseEntity.ok(response);
    }

    /*
     * GET LOAN ACCOUNT BY DEAL ID
     *
     * Example:
     * GET /api/v1/loan-accounts/deal/8
     */
    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    getLoanAccountByDealId(
            @PathVariable Integer dealId) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.getLoanAccountByDealId(dealId);

        return ResponseEntity.ok(response);
    }

    /*
     * GET LATEST LOAN ACCOUNT FOR CUSTOMER
     *
     * Example:
     * GET /api/v1/loan-accounts/customer/10/latest
     */
    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<ResponseApi<LoanAccountResponse>>
    getLatestLoanAccountByCustomerId(
            @PathVariable Integer customerId) {

        ResponseApi<LoanAccountResponse> response =
                loanAccountService.getLatestLoanAccountByCustomerId(
                        customerId
                );

        return ResponseEntity.ok(response);
    }

    /*
     * GET LOAN ACCOUNTS FOR A CUSTOMER WITH PAGINATION
     *
     * Example:
     * GET /api/v1/loan-accounts/customer/10?page=0&size=10
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<
            ResponseApi<PageResponse<LoanAccountResponse>>>
    getLoanAccountsByCustomerId(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResponseApi<PageResponse<LoanAccountResponse>> response =
                loanAccountService.getLoanAccountsByCustomerId(
                        customerId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}
