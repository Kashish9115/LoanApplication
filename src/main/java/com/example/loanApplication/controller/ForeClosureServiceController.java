package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.CustomerForeClosureRequestDto;
import com.example.loanApplication.dto.FullClosureRequestDto;
import com.example.loanApplication.dto.LoanAccountDetailsDto;
import com.example.loanApplication.dto.PartialClosureRequestDto;
import com.example.loanApplication.entity.ForeClosureRequest;
import com.example.loanApplication.service.ForeClosureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class ForeClosureServiceController {

    private  final ForeClosureService foreClosureService;
    @GetMapping("/loanAccount/getAllActiveLoanAccount")
    public ResponseApi<List<Integer>> getAllActiveLoanAccount(
            @RequestParam Integer customerId) {

        return foreClosureService.getAllActiveLoanAccounts(customerId);
    }

    @GetMapping("/loanAccount/{loanAccountId}")
    public ResponseApi<LoanAccountDetailsDto> getLoanAccountDetails(
            @PathVariable Integer loanAccountId) {

        return foreClosureService.getLoanAccountDetails(loanAccountId);
    }

    @PostMapping("/loanAccount/{loanAccountId}/fullclosure")
    public ResponseApi<ForeClosureRequest> requestFullClosure(
            @PathVariable Integer loanAccountId,
            @RequestBody FullClosureRequestDto request) {

        return foreClosureService.requestFullClosure(
                loanAccountId,
                request
        );
    }
    @PostMapping("/loanAccount/{loanAccountId}/partialclosure")
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


}
