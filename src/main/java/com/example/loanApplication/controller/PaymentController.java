package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.ForeClosurePaymentRequestDto;
import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;
import com.example.loanApplication.dto.RazorpayOrderResponseDto;
import com.example.loanApplication.service.LoanPaymentService;
import com.example.loanApplication.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.index.qual.SameLen;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {


    private final RazorpayService razorpayService; // Gateway Service
    private final LoanPaymentService paymentService;   // Domain Ledger Service



    @PostMapping("/create-order/{emiScheduleId}")
    public ResponseEntity<RazorpayOrderResponseDto> createOrder(
            @PathVariable Integer emiScheduleId) {

        log.info("REST Request to create Razorpay Order for EMI Schedule ID: {}",
                emiScheduleId);

        RazorpayOrderResponseDto orderResponse =
                razorpayService.createOrderForEmiSchedule(emiScheduleId);

        return ResponseEntity.ok(orderResponse);
    }











    @PostMapping("/verify-and-pay")
    public ResponseEntity<LoanPaymentResponseDTO> verifyAndPay(
            @RequestBody LoanPaymentRequestDTO requestDto) {

        log.info("REST Request to verify payment and update ledgers for LoanAccountId: {}",
                requestDto.getLoanAccountId());

        razorpayService.verifySignature(
                requestDto.getRazorpayOrderId(),
                requestDto.getRazorpayPaymentId(),
                requestDto.getRazorpaySignature()
        );

        LoanPaymentResponseDTO response =
                paymentService.processPaymentLedger(requestDto);

        return ResponseEntity.ok(response);
    }






    @GetMapping("/history/customer/{loanAccountId}")
    public ResponseEntity<List<LoanPaymentResponseDTO>> getCustomerPaymentHistory(
            @PathVariable Integer loanAccountId) {

        log.info("REST Request to fetch payment history for LoanAccountId: {}",
                loanAccountId);

        List<LoanPaymentResponseDTO> history =
                paymentService.getPaymentHistoryByAccountId(loanAccountId);

        return ResponseEntity.ok(history);
    }




    @GetMapping("/history/admin/all")
    public ResponseEntity<List<LoanPaymentResponseDTO>> getAllPaymentsForAdmin() {

        log.info("REST Request to fetch all payments for Admin view");

        List<LoanPaymentResponseDTO> allPayments =
                paymentService.getAllPaymentsForAdmin();

        return ResponseEntity.ok(allPayments);
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


    @PostMapping("/create-order/overdue/{loanAccountId}")
    public ResponseEntity<RazorpayOrderResponseDto> createOverdueOrder(
            @PathVariable Integer loanAccountId) {

        return ResponseEntity.ok(
                razorpayService.createOrderForOverdueEmis(loanAccountId)
        );
    }

}
