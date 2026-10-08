package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.EligibilityRequest;
import com.example.loanApplication.dto.EligibilityResponse;
import com.example.loanApplication.service.EligibilityService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eligibility")
@RequiredArgsConstructor
public class EligibilityController {

    private final EligibilityService eligibilityService;

    @PostMapping("/calculate")
    public ResponseEntity<ResponseApi<EligibilityResponse>> calculateEligibility(
            @Valid @RequestBody EligibilityRequest request
    ) {

        ResponseApi<EligibilityResponse> response =
                eligibilityService.calculateEligibility(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<ResponseApi<EligibilityResponse>> getLatestEligibility(
            @PathVariable Integer customerId
    ) {

        ResponseApi<EligibilityResponse> response =
                eligibilityService.getLatestEligibility(customerId);

        return ResponseEntity.ok(response);
    }
}