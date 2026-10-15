package com.example.loanApplication.controller.dashboard;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.dashboard.AdminDashboardSummaryDto;
import com.example.loanApplication.service.AdminDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ResponseApi<AdminDashboardSummaryDto>> getDashboardSummary() {

        AdminDashboardSummaryDto summary = dashboardService.getDashboardSummary();

        return ResponseEntity.ok(
                ResponseApi.<AdminDashboardSummaryDto>builder()
                        .success(true)
                        .message("Admin Dashboard summary fetched successfully.")
                        .data(summary)
                        .build()
        );
    }
}