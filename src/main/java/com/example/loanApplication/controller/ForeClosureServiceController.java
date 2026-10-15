package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.entity.ForeClosureRequest;
import com.example.loanApplication.service.ForeClosureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")

@RequiredArgsConstructor
public class ForeClosureServiceController {

    private  final ForeClosureService foreClosureService;
    @GetMapping("/customer/loanAccount/getAllActiveLoanAccount")
    public ResponseApi<List<Integer>> getAllActiveLoanAccount(
            @RequestParam Integer customerId) {

        return foreClosureService.getAllActiveLoanAccounts(customerId);
    }

    @GetMapping("/customer/loanAccount/{loanAccountId}")
    public ResponseApi<LoanAccountDetailsDto> getLoanAccountDetails(
            @PathVariable Integer loanAccountId) {

        return foreClosureService.getLoanAccountDetails(loanAccountId);
    }

    @PostMapping("/customer/loanAccount/{loanAccountId}/fullclosure")
    public ResponseApi<ForeClosureRequest> requestFullClosure(
            @PathVariable Integer loanAccountId,
            @RequestBody FullClosureRequestDto request) {

        return foreClosureService.requestFullClosure(
                loanAccountId,
                request
        );
    }
    @PostMapping("/customer/loanAccount/{loanAccountId}/partialclosure")
    public ResponseApi<ForeClosureRequest> requestPartialClosure(
            @PathVariable Integer loanAccountId,
            @RequestBody PartialClosureRequestDto request) {

        return foreClosureService.requestPartialClosure(
                loanAccountId,
                request
        );
    }
    @GetMapping("/customer/foreclosure/requests")
    public ResponseApi<List<CustomerForeClosureRequestDto>>
    getCustomerForeclosureRequests(
            @RequestParam Integer customerId) {

        return foreClosureService.getCustomerForeclosureRequests(customerId);
    }
    @GetMapping("/officer/foreclosure/requests/pending")
    public ResponseApi<List<ForeClosureRequest>>
    getPendingForeclosureRequests() {

        return foreClosureService.getPendingForeclosureRequests();
    }


    @PatchMapping("/officer/foreclosure/requests/{requestId}/approve")
    public ResponseApi<ForeClosureRequest> approveForeclosureRequest(
            @PathVariable Integer requestId) {

        return foreClosureService.approveForeclosureRequest(requestId);
    }


    @PatchMapping("/officer/foreclosure/requests/{requestId}/reject")
    public ResponseApi<ForeClosureRequest> rejectForeclosureRequest(
            @PathVariable Integer requestId) {

        return foreClosureService.rejectForeclosureRequest(requestId);
    }

    @GetMapping("/customer/foreclosure/payments")
    public ResponseApi<List<ForeClosurePaymentDto>>
    getApprovedForeclosurePayments(
            @RequestParam Integer customerId) {

        return foreClosureService
                .getApprovedForeclosurePayments(customerId);
    }
    @PostMapping("/customer/foreclosure/pay")
    public ResponseApi<LoanPaymentResponseDTO> processForeClosurePayment(
            @RequestBody ForeClosurePaymentRequestDto dto) {

        LoanPaymentResponseDTO response =
                foreClosureService.processForeClosurePayment(dto);

        return new ResponseApi<>(
                true,
                "Foreclosure payment processed successfully",
                response
        );
    }
//    @GetMapping("/customer/foreclosure/requests")
//    public ResponseApi<Page<CustomerForeClosureRequestDto>>
//    getCustomerForeclosureRequests(
//            @RequestParam Integer customerId,
//            @PageableDefault(size = 5, sort = "requestId",
//                    direction = Sort.Direction.DESC)
//            Pageable pageable) {
//
//        return foreClosureService
//                .getCustomerForeclosureRequests(customerId, pageable);
//    }
}
