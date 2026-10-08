package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.DealReviewRequest;
import com.example.loanApplication.dto.DealReviewResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.service.DealReviewService;

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
@RequestMapping("/api/v1/deal-reviews")
@RequiredArgsConstructor
public class DealReviewController {

    private final DealReviewService dealReviewService;


    @PostMapping
    public ResponseEntity<ResponseApi<DealReviewResponse>> reviewLoanDeal(
            @Valid @RequestBody DealReviewRequest request
    ) {

        ResponseApi<DealReviewResponse> response =
                dealReviewService.reviewLoanDeal(request);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/deal/{dealId}/latest")
    public ResponseEntity<ResponseApi<DealReviewResponse>>
    getLatestReviewByDealId(
            @PathVariable Integer dealId
    ) {

        ResponseApi<DealReviewResponse> response =
                dealReviewService.getLatestReviewByDealId(
                        dealId
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/deal/{dealId}")
    public ResponseEntity<ResponseApi<PageResponse<DealReviewResponse>>>
    getReviewsByDealId(
            @PathVariable Integer dealId,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size
    ) {

        ResponseApi<PageResponse<DealReviewResponse>> response =
                dealReviewService.getReviewsByDealId(
                        dealId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }


    @GetMapping("/officer/{officerId}")
    public ResponseEntity<ResponseApi<PageResponse<DealReviewResponse>>>
    getReviewsByOfficerId(
            @PathVariable Integer officerId,

            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "10"
            )
            int size
    ) {

        ResponseApi<PageResponse<DealReviewResponse>> response =
                dealReviewService.getReviewsByOfficerId(
                        officerId,
                        page,
                        size
                );

        return ResponseEntity.ok(response);
    }
}