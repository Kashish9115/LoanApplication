package com.example.loanApplication.service;

import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.dashboard.AdminDashboardSummaryDto;
import com.example.loanApplication.dto.report.CibilScoreRangeReportDto;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AdminDashboardService {

    AdminDashboardSummaryDto getDashboardSummary();


}
