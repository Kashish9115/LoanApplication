package com.example.loanApplication.controller;

import com.example.loanApplication.dto.LoanPaymentRequestDTO;
import com.example.loanApplication.dto.LoanPaymentResponseDTO;
import com.example.loanApplication.dto.RazorpayOrderResponseDto;
import com.example.loanApplication.service.LoanPaymentService;
import com.example.loanApplication.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {


    private final RazorpayService razorpayService; // Gateway Service
    private final LoanPaymentService paymentService;   // Domain Ledger Service

    /**
     * Step 1: Create Razorpay Order ID for Frontend Checkout Modal
     */
    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponseDto> createRazorpayOrder(
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "INR") String currency) {

        RazorpayOrderResponseDto order = razorpayService.createOrder(amount, currency);
        return ResponseEntity.ok(order);
    }

    /**
     * Step 2: Verify Razorpay Signature AND Process Database Ledger Update
     */
    @PostMapping("/verify-and-pay")
    public ResponseEntity<LoanPaymentResponseDTO> verifyAndPay(@RequestBody LoanPaymentRequestDTO requestDto) {

        // 1. Gateway verification check
        razorpayService.verifySignature(
                requestDto.getRazorpayOrderId(),
                requestDto.getRazorpayPaymentId(),
                requestDto.getRazorpaySignature()
        );

        // 2. Database ledger update (Only executes if signature verification succeeds)
        LoanPaymentResponseDTO response = paymentService.processPayment(requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 3: Get Payment Transaction History
     */
    @GetMapping("/history/{loanAccountId}")
    public ResponseEntity<List<LoanPaymentResponseDTO>> getPaymentHistory(@PathVariable Integer loanAccountId) {
        List<LoanPaymentResponseDTO> history = paymentService.getPaymentHistoryByAccountId(loanAccountId);
        return ResponseEntity.ok(history);
    }



}
