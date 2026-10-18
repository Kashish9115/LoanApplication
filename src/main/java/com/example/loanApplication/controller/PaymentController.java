package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.*;
import com.example.loanApplication.service.EmiScheduleService;
import com.example.loanApplication.service.LoanPaymentService;
import com.example.loanApplication.service.RazorpayService;
import com.example.loanApplication.serviceImpl.EmiScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@RestController
@CrossOrigin(origins =  "http://localhost:4200")
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {


    private final RazorpayService razorpayService; // Gateway Service
    private final LoanPaymentService paymentService;   // Domain Ledger Service
 private  final EmiScheduleServiceImpl emiScheduleService;


    @PostMapping("/create-order/{emiScheduleId}")
    public ResponseEntity<RazorpayOrderResponseDto> createOrder(
            @PathVariable Integer emiScheduleId) {

        log.info("REST Request to create Razorpay Order for EMI Schedule ID: {}",
                emiScheduleId);

        RazorpayOrderResponseDto orderResponse =
                razorpayService.createOrderForEmiSchedule(emiScheduleId);

        return ResponseEntity.ok(orderResponse);
    }











//    @PostMapping("/verify-and-pay")
//    public ResponseEntity<LoanPaymentResponseDTO> verifyAndPay(
//            @RequestBody LoanPaymentRequestDTO requestDto) {
//        razorpayService.verifySignature(
//                requestDto.getRazorpayOrderId(),
//                requestDto.getRazorpayPaymentId(),
//                requestDto.getRazorpaySignature()
//        );
//
//        LoanPaymentResponseDTO response =
//                paymentService.processPaymentLedger(requestDto);
//
//        return ResponseEntity.ok(response);
//    }




    @PostMapping("/verify-and-pay")
    public ResponseEntity<LoanPaymentResponseDTO> verifyAndPay(
            @RequestBody LoanPaymentRequestDTO requestDto) {
        if (requestDto.getEmiScheduleId() == null) {
            throw new IllegalArgumentException("EMI schedule ID is required for Razorpay EMI verification.");
        }
        List<EmiBreakDownDto> breakdowns =
                emiScheduleService.getPendingPaymentBreakdownsThrough(requestDto.getEmiScheduleId());
        BigDecimal requiredAmount = breakdowns.stream()
                .map(EmiBreakDownDto::getTotalPayableAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        razorpayService.verifyCapturedPayment(
                requestDto.getRazorpayOrderId(),
                requestDto.getRazorpayPaymentId(),
                requestDto.getRazorpaySignature(),
                requiredAmount
        );

        // Force the ledger to use the server-calculated amount rather than a client value.
        requestDto.setPaymentAmount(requiredAmount);
        requestDto.setLoanAccountId(breakdowns.get(breakdowns.size() - 1).getLoanAccountId());
        LoanPaymentResponseDTO response = paymentService.processPaymentLedger(requestDto);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/history/customer/{loanAccountId}")
    public ResponseEntity<PageResponse<PaymentHistoryResponseDTO>> getCustomerPaymentHistory(
            @PathVariable Integer loanAccountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse< PaymentHistoryResponseDTO> history =
                paymentService.getPaymentHistoryByAccountId(
                        loanAccountId, page, size);

        return ResponseEntity.ok(history);
    }





    @GetMapping("/history/admin/all")
    public ResponseEntity<PageResponse<PaymentHistoryResponseDTO>> getAllPaymentsForAdmin(@RequestParam(defaultValue = "0") int page , @RequestParam(defaultValue = "5")int size) {

        return ResponseEntity.ok(paymentService.getAllPaymentsForAdmin(page, size));
    }




    @PostMapping("/customer/foreclosure/pay")
    public ResponseApi<LoanPaymentResponseDTO> processForeClosurePayment(
            @RequestBody ForeClosurePaymentRequestDto dto) {

        LoanPaymentResponseDTO response =
                paymentService.processForeClosurePayment(dto);

        return new ResponseApi<>(
                true,
                "Foreclosure payment processed successfully",
                response
        );
    }


    @PostMapping("/create-order/overdueemis/{loanAccountId}")
    public ResponseEntity<RazorpayOrderResponseDto> createOverdueOrder(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                razorpayService.createOrderForOverdueEmis(loanAccountId)
        );
    }

}
