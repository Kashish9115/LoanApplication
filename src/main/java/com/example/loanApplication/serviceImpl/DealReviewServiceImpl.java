
package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.DealReviewRequest;
import com.example.loanApplication.dto.DealReviewResponse;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.entity.DealReview;
import com.example.loanApplication.entity.LoanDeal;
import com.example.loanApplication.enumeration.LoanDealStatus;
import com.example.loanApplication.enumeration.ReviewStatus;
import com.example.loanApplication.exception.InvalidLoanDealException;
import com.example.loanApplication.exception.ResourceNotFoundException;
import com.example.loanApplication.repository.DealReviewRepository;
import com.example.loanApplication.repository.LoanDealRepository;
import com.example.loanApplication.service.DealReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DealReviewServiceImpl implements DealReviewService {

    private final DealReviewRepository dealReviewRepository;
    private final LoanDealRepository loanDealRepository;

    @Override
    @Transactional
    public ResponseApi<DealReviewResponse> reviewLoanDeal(
            DealReviewRequest request
    ) {

        log.info(
                "Starting loan deal review. dealId: {}, officerId: {}, requestedStatus: {}",
                request.getDealId(),
                request.getOfficerId(),
                request.getStatus()
        );

        LoanDeal loanDeal =
                loanDealRepository
                        .findById(request.getDealId())
                        .orElseThrow(() -> {
                            log.warn(
                                    "Loan deal not found for review. dealId: {}",
                                    request.getDealId()
                            );
                            return new ResourceNotFoundException(
                                    "Loan deal not found with ID: "
                                            + request.getDealId()
                            );
                        });

        if (loanDeal.getDealStatus()
                != LoanDealStatus.MANUAL_REVIEW) {

            log.warn(
                    "Loan deal review rejected because deal is not in MANUAL_REVIEW status. dealId: {}, currentStatus: {}",
                    loanDeal.getDealId(),
                    loanDeal.getDealStatus()
            );

            throw new InvalidLoanDealException(
                    "Only loan deals in MANUAL_REVIEW status can be reviewed."
            );
        }

        ReviewStatus requestedStatus = request.getStatus();

        if (requestedStatus != ReviewStatus.APPROVED
                && requestedStatus != ReviewStatus.REJECTED) {

            log.warn(
                    "Invalid review status requested for dealId: {}, status: {}",
                    request.getDealId(),
                    requestedStatus
            );

            throw new InvalidLoanDealException(
                    "Review status must be APPROVED or REJECTED."
            );
        }

        DealReview dealReview =
                DealReview.builder()
                        .dealId(loanDeal.getDealId())
                        .officerId(request.getOfficerId())
                        .status(requestedStatus)
                        .build();

        DealReview savedReview =
                dealReviewRepository.save(dealReview);

        log.info(
                "Deal review saved. reviewId: {}, dealId: {}, officerId: {}, status: {}",
                savedReview.getReviewId(),
                savedReview.getDealId(),
                savedReview.getOfficerId(),
                savedReview.getStatus()
        );

        String responseMessage;

        if (requestedStatus == ReviewStatus.APPROVED) {

            loanDeal.setDealStatus(LoanDealStatus.APPROVED);
            loanDeal.setApprovedAmount(loanDeal.getLoanAmount());

            responseMessage = "Loan deal approved successfully";

            log.info(
                    "Loan deal approved. dealId: {}, approvedAmount: {}",
                    loanDeal.getDealId(),
                    loanDeal.getApprovedAmount()
            );

        } else {

            loanDeal.setDealStatus(LoanDealStatus.REJECTED);
            loanDeal.setApprovedAmount(null);

            responseMessage = "Loan deal rejected successfully";

            log.info(
                    "Loan deal rejected. dealId: {}, officerId: {}",
                    loanDeal.getDealId(),
                    request.getOfficerId()
            );
        }

        LoanDeal updatedLoanDeal =
                loanDealRepository.save(loanDeal);

        log.info(
                "Loan deal status updated successfully. dealId: {}, status: {}",
                updatedLoanDeal.getDealId(),
                updatedLoanDeal.getDealStatus()
        );

        DealReviewResponse response =
                buildReviewResponse(
                        savedReview,
                        updatedLoanDeal,
                        responseMessage
                );

        return ResponseApi
                .<DealReviewResponse>builder()
                .success(true)
                .message(responseMessage)
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<DealReviewResponse> getLatestReviewByDealId(
            Integer dealId
    ) {

        log.info("Fetching latest review for dealId: {}", dealId);

        DealReview dealReview =
                dealReviewRepository
                        .findTopByDealIdOrderByReviewIdDesc(dealId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Review not found for dealId: {}",
                                    dealId
                            );
                            return new ResourceNotFoundException(
                                    "Review not found for loan deal: "
                                            + dealId
                            );
                        });

        LoanDeal loanDeal =
                loanDealRepository
                        .findById(dealId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Loan deal not found while fetching review. dealId: {}",
                                    dealId
                            );
                            return new ResourceNotFoundException(
                                    "Loan deal not found with ID: "
                                            + dealId
                            );
                        });

        DealReviewResponse response =
                buildReviewResponse(
                        dealReview,
                        loanDeal,
                        getReviewMessage(dealReview.getStatus())
                );

        log.info(
                "Latest review fetched successfully. reviewId: {}, dealId: {}, status: {}",
                dealReview.getReviewId(),
                dealId,
                dealReview.getStatus()
        );

        return ResponseApi
                .<DealReviewResponse>builder()
                .success(true)
                .message("Latest deal review fetched successfully")
                .data(response)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<DealReviewResponse>>
    getReviewsByDealId(
            Integer dealId,
            int page,
            int size
    ) {

        log.info(
                "Fetching reviews for dealId: {}, page: {}, size: {}",
                dealId,
                page,
                size
        );

        validatePagination(page, size);

        LoanDeal loanDeal =
                loanDealRepository
                        .findById(dealId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Loan deal not found while fetching reviews. dealId: {}",
                                    dealId
                            );
                            return new ResourceNotFoundException(
                                    "Loan deal not found with ID: "
                                            + dealId
                            );
                        });

        Pageable pageable = PageRequest.of(page, size);

        Page<DealReview> reviewPage =
                dealReviewRepository
                        .findByDealIdOrderByReviewIdDesc(
                                dealId,
                                pageable
                        );

        if (reviewPage.isEmpty()) {
            log.warn("No reviews found for dealId: {}", dealId);

            throw new ResourceNotFoundException(
                    "No reviews found for loan deal: " + dealId
            );
        }

        List<DealReviewResponse> content =
                reviewPage.getContent()
                        .stream()
                        .map(review ->
                                buildReviewResponse(
                                        review,
                                        loanDeal,
                                        getReviewMessage(review.getStatus())
                                )
                        )
                        .toList();

        PageResponse<DealReviewResponse> pageResponse =
                PageResponse.<DealReviewResponse>builder()
                        .content(content)
                        .pageNumber(reviewPage.getNumber())
                        .pageSize(reviewPage.getSize())
                        .totalElements(reviewPage.getTotalElements())
                        .totalPages(reviewPage.getTotalPages())
                        .first(reviewPage.isFirst())
                        .last(reviewPage.isLast())
                        .build();

        log.info(
                "Reviews fetched for dealId: {}, returnedRecords: {}, totalElements: {}",
                dealId,
                content.size(),
                reviewPage.getTotalElements()
        );

        return ResponseApi
                .<PageResponse<DealReviewResponse>>builder()
                .success(true)
                .message("Deal reviews fetched successfully")
                .data(pageResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseApi<PageResponse<DealReviewResponse>>
    getReviewsByOfficerId(
            Integer officerId,
            int page,
            int size
    ) {

        log.info(
                "Fetching deal reviews for officerId: {}, page: {}, size: {}",
                officerId,
                page,
                size
        );

        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<DealReview> reviewPage =
                dealReviewRepository
                        .findByOfficerIdOrderByReviewIdDesc(
                                officerId,
                                pageable
                        );

        if (reviewPage.isEmpty()) {
            log.warn(
                    "No deal reviews found for officerId: {}",
                    officerId
            );

            throw new ResourceNotFoundException(
                    "No deal reviews found for officer: " + officerId
            );
        }

        List<DealReviewResponse> content =
                reviewPage.getContent()
                        .stream()
                        .map(review -> {

                            LoanDeal loanDeal =
                                    loanDealRepository
                                            .findById(review.getDealId())
                                            .orElseThrow(() -> {
                                                log.warn(
                                                        "Loan deal not found for reviewId: {}, dealId: {}",
                                                        review.getReviewId(),
                                                        review.getDealId()
                                                );
                                                return new ResourceNotFoundException(
                                                        "Loan deal not found with ID: "
                                                                + review.getDealId()
                                                );
                                            });

                            return buildReviewResponse(
                                    review,
                                    loanDeal,
                                    getReviewMessage(review.getStatus())
                            );
                        })
                        .toList();

        PageResponse<DealReviewResponse> pageResponse =
                PageResponse.<DealReviewResponse>builder()
                        .content(content)
                        .pageNumber(reviewPage.getNumber())
                        .pageSize(reviewPage.getSize())
                        .totalElements(reviewPage.getTotalElements())
                        .totalPages(reviewPage.getTotalPages())
                        .first(reviewPage.isFirst())
                        .last(reviewPage.isLast())
                        .build();

        log.info(
                "Officer reviews fetched successfully. officerId: {}, returnedRecords: {}, totalElements: {}",
                officerId,
                content.size(),
                reviewPage.getTotalElements()
        );

        return ResponseApi
                .<PageResponse<DealReviewResponse>>builder()
                .success(true)
                .message("Officer deal reviews fetched successfully")
                .data(pageResponse)
                .build();
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            log.warn(
                    "Invalid pagination: page number cannot be negative. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page number cannot be negative."
            );
        }

        if (size <= 0) {
            log.warn(
                    "Invalid pagination: page size must be positive. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page size must be greater than zero."
            );
        }

        if (size > 100) {
            log.warn(
                    "Invalid pagination: page size exceeds maximum. page: {}, size: {}",
                    page,
                    size
            );

            throw new InvalidLoanDealException(
                    "Page size cannot be greater than 100."
            );
        }
    }

    private DealReviewResponse buildReviewResponse(
            DealReview dealReview,
            LoanDeal loanDeal,
            String message
    ) {

        return DealReviewResponse.builder()
                .reviewId(dealReview.getReviewId())
                .dealId(dealReview.getDealId())
                .customerId(loanDeal.getCustomerId())
                .officerId(dealReview.getOfficerId())
                .reviewStatus(dealReview.getStatus())
                .dealStatus(loanDeal.getDealStatus())
                .requestedLoanAmount(loanDeal.getLoanAmount())
                .approvedAmount(loanDeal.getApprovedAmount())
                .message(message)
                .build();
    }

    private String getReviewMessage(ReviewStatus reviewStatus) {

        if (reviewStatus == ReviewStatus.APPROVED) {
            return "Loan deal approved successfully";
        }

        if (reviewStatus == ReviewStatus.REJECTED) {
            return "Loan deal rejected";
        }

        return "Loan deal review pending";
    }
}
