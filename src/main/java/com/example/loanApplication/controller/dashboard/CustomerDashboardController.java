package com.example.loanApplication.controller.dashboard;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.dashboard.CustomerDashboardDto;
import com.example.loanApplication.service.CustomerDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customer/dashboard")
public class CustomerDashboardController {

    @Autowired
    private CustomerDashboardService customerDashboardService;

    @GetMapping("/{customerId}")
    public ResponseEntity<ResponseApi<CustomerDashboardDto>> getCustomerDashboard(
            @PathVariable Integer customerId
    ) {

        CustomerDashboardDto dashboard =
                customerDashboardService.getCustomerDashboard(customerId);

        return ResponseEntity.ok(
                ResponseApi.<CustomerDashboardDto>builder()
                        .success(true)
                        .message("Customer Dashboard fetched successfully.")
                        .data(dashboard)
                        .build()
        );
    }
}