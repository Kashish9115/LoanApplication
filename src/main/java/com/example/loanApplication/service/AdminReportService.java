package com.example.loanApplication.service;

import com.example.loanApplication.dto.report.ProductWiseReportDto;
import com.example.loanApplication.dto.report.TurnAroundTimeReportDto;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.report.CibilScoreRangeReportDto;
import com.example.loanApplication.dto.report.ScoreCardWiseReportDto;

import java.time.LocalDateTime;

public interface AdminReportService {


    PageResponse<CibilScoreRangeReportDto> getCibilScoreRangeReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    );

    PageResponse<ScoreCardWiseReportDto> getScoreCardWiseReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    );


    PageResponse<ProductWiseReportDto> getProductWiseReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    );

    PageResponse<TurnAroundTimeReportDto> getTurnAroundTimeReport(
            LocalDateTime fromDate,
            LocalDateTime toDate,
            int page,
            int size
    );

}
