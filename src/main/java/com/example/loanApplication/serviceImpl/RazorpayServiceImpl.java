package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.EmiBreakDownDto;
import com.example.loanApplication.dto.OverdueEmiPaymentBreakdownDto;
import com.example.loanApplication.dto.RazorpayOrderResponseDto;
import com.example.loanApplication.exception.SignatureVerificationException;
import com.example.loanApplication.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl  implements RazorpayService {


    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String keySecret;
    private final EmiScheduleServiceImpl emiService;


//
// yeh wala sahi hai
//    @Override
//    public boolean verifySignature(
//            String orderId,
//            String paymentId,
//            String signature) {
//
//        if (orderId == null || orderId.isBlank()
//                || paymentId == null || paymentId.isBlank()
//                || signature == null || signature.isBlank()) {
//
//            throw new SignatureVerificationException(
//                    "Razorpay order ID, payment ID and signature are required."
//            );
//        }
//
//        try {
//            JSONObject attributes = new JSONObject();
//            attributes.put("razorpay_order_id", orderId);
//            attributes.put("razorpay_payment_id", paymentId);
//            attributes.put("razorpay_signature", signature);
//
//            boolean valid = Utils.verifyPaymentSignature(
//                    attributes, keySecret
//            );
//
//            if (!valid) {
//                throw new SignatureVerificationException(
//                        "Invalid Razorpay payment signature."
//                );
//            }
//
//            return true;
//
//        } catch (SignatureVerificationException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new SignatureVerificationException(
//                    "Razorpay signature verification failed."
//            );
//        }
//    }
//
//
//
//














































    @Override
    public RazorpayOrderResponseDto createOrderForOverdueEmis(
            Integer loanAccountId) {

        OverdueEmiPaymentBreakdownDto breakdown =
                emiService.getAllOverdueEmiBreakdown(loanAccountId);

        if (breakdown.getOverdueEmis() == null
                || breakdown.getOverdueEmis().isEmpty()) {
            throw new IllegalStateException(
                    "No overdue EMI available for payment."
            );
        }

        BigDecimal amount = breakdown.getTotalPayableAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Invalid overdue payment amount."
            );
        }

        try {
            JSONObject orderRequest = new JSONObject();

            orderRequest.put(
                    "amount",
                    amount.multiply(new BigDecimal("100"))
                            .intValueExact()
            );

            orderRequest.put("currency", "INR");

            Order order = razorpayClient.orders.create(orderRequest);

            return new RazorpayOrderResponseDto(
                    order.get("id"),
                    "INR",
                    amount,
                    null,
                    loanAccountId
            );

        } catch (Exception e) {
            log.error("Overdue EMI order creation failed", e);
            throw new RuntimeException(
                    "Unable to create overdue EMI payment order."
            );
        }
    }

    @Value("${razorpay.signature.mock-enabled:false}")
    private boolean mockSignatureEnabled;

    @Override
    public boolean verifySignature(
            String orderId,
            String paymentId,
            String signature) {

        // Local Postman testing only
        if (mockSignatureEnabled) {
            log.warn("Signature verification bypassed for local testing");
            return true;
        }

        if (orderId == null || orderId.isBlank()
                || paymentId == null || paymentId.isBlank()
                || signature == null || signature.isBlank()) {
            throw new SignatureVerificationException(
                    "Razorpay order ID, payment ID and signature are required."
            );
        }

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);

            boolean valid = Utils.verifyPaymentSignature(attributes, keySecret);

            if (!valid) {
                throw new SignatureVerificationException(
                        "Invalid Razorpay payment signature."
                );
            }

            return true;
        } catch (SignatureVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new SignatureVerificationException(
                    "Razorpay signature verification failed."
            );
        }
    }





    @Override
    public RazorpayOrderResponseDto createOrderForEmiSchedule(
            Integer emiScheduleId) {

        EmiBreakDownDto breakdown =
                emiService.getEmiBreakdown(emiScheduleId);

        if ("PAID".equalsIgnoreCase(breakdown.getPaymentStatus())) {
            throw new IllegalStateException(
                    "This EMI installment is already PAID."
            );
        }

        BigDecimal amount = breakdown.getTotalPayableAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Invalid EMI payment amount."
            );
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put(
                    "amount",
                    amount.multiply(new BigDecimal("100")).intValueExact()
            );
            orderRequest.put("currency", "INR");

            Order order = razorpayClient.orders.create(orderRequest);

            return new RazorpayOrderResponseDto(
                    order.get("id"),
                    "INR",
                    amount,
                    emiScheduleId,
                    breakdown.getLoanAccountId()
            );

        } catch (Exception e) {
            log.error("Razorpay order creation failed", e);
            throw new RuntimeException(
                    "Failed to generate Razorpay order.", e
            );
        }
    }
}
