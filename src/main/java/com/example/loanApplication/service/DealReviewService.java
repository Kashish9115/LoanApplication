package com.example.loanApplication.service;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.DealReviewRequest;
import com.example.loanApplication.dto.DealReviewResponse;
import com.example.loanApplication.dto.PageResponse;

public interface DealReviewService {

    ResponseApi<DealReviewResponse> reviewLoanDeal(
            DealReviewRequest request
    );

    ResponseApi<DealReviewResponse> getLatestReviewByDealId(
            Integer dealId
    );

    ResponseApi<PageResponse<DealReviewResponse>> getReviewsByDealId(
            Integer dealId,
            int page,
            int size
    );

    ResponseApi<PageResponse<DealReviewResponse>> getReviewsByOfficerId(
            Integer officerId,
            int page,
            int size
    );

}