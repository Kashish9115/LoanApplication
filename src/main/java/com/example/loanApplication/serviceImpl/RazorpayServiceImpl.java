package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.dto.RazorpayOrderResponseDto;
import com.example.loanApplication.exception.SignatureVerificationException;
import com.example.loanApplication.service.RazorpayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RazorpayServiceImpl  implements RazorpayService {


    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Override
    public RazorpayOrderResponseDto createOrder(BigDecimal amount, String currency) {



        try {
            JSONObject orderRequest = new JSONObject();
            // Convert Amount to Paise (e.g. ₹100.00 -> 10000 paise)
            orderRequest.put("amount", amount.multiply(new BigDecimal("100")).intValue());
            orderRequest.put("currency", currency != null ? currency : "INR");
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");

            return new RazorpayOrderResponseDto(orderId,  currency , amount);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay Order: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String orderId, String paymentId, String signature) {


        // 👈 1. LOCAL TESTING BYPASS: Agar signature "dummy_signature" ho toh validation skip ho jayegi
        if ("dummy_signature".equalsIgnoreCase(signature) || "bypass".equalsIgnoreCase(signature)) {
            return true;
        }


        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", orderId);
            attributes.put("razorpay_payment_id", paymentId);
            attributes.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentSignature(attributes, keySecret);
            if (!isValid) {
                throw new SignatureVerificationException("Invalid HMAC-SHA256 signature. Payment verification failed.");
            }
            return true;
        } catch (Exception e) {
            throw new SignatureVerificationException("Signature verification exception: " + e.getMessage());
        }
    }


}
