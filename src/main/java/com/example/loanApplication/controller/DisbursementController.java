package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.DisbursementRequest;
import com.example.loanApplication.dto.DisbursementResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.service.DisbursementService;

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
@RequestMapping("/api/v1/disbursements")
@RequiredArgsConstructor
public class DisbursementController {

    private final DisbursementService disbursementService;


    @PostMapping
    public ResponseEntity<ResponseApi<DisbursementResponse>>
    createDisbursement(
            @Valid @RequestBody DisbursementRequest request
    ) {

        ResponseApi<DisbursementResponse> response =
                disbursementService.createDisbursement(request);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{disbursementId}/complete")
    public ResponseEntity<ResponseApi<DisbursementResponse>>
    completeDisbursement(
            @PathVariable Integer disbursementId
    ) {

        ResponseApi<DisbursementResponse> response =
                disbursementService.completeDisbursement(
                        disbursementId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{disbursementId}")
    public ResponseEntity<ResponseApi<DisbursementResponse>>
    getDisbursementById(
            @PathVariable Integer disbursementId
    ) {

        ResponseApi<DisbursementResponse> response =
                disbursementService.getDisbursementById(
                        disbursementId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/deal/{dealId}/latest")
    public ResponseEntity<ResponseApi<DisbursementResponse>>
    getLatestDisbursementByDealId(
            @PathVariable Integer dealId
    ) {

        ResponseApi<DisbursementResponse> response =
                disbursementService.getLatestDisbursementByDealId(
                        dealId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<PageResponse<DisbursementResponse>>>
    getDisbursementsByDealId(
            @PathVariable Integer dealId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        ResponseApi<PageResponse<DisbursementResponse>> response =
                disbursementService.getDisbursementsByDealId(
                        dealId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}