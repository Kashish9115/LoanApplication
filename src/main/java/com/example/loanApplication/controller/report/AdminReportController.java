package com.example.loanApplication.controller.report;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.PageResponse;
import com.example.loanApplication.dto.report.CibilScoreRangeReportDto;
import com.example.loanApplication.dto.report.ProductWiseReportDto;
import com.example.loanApplication.dto.report.ScoreCardWiseReportDto;
import com.example.loanApplication.dto.report.TurnAroundTimeReportDto;
import com.example.loanApplication.service.AdminReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminReportController {

    @Autowired
    private AdminReportService reportService;

    @GetMapping("/cibil-score-range")
    public ResponseEntity<ResponseApi<PageResponse<CibilScoreRangeReportDto>>> getCibilScoreRangeReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size
    ) {
        PageResponse<CibilScoreRangeReportDto> report =
                reportService.getCibilScoreRangeReport(
                        fromDate,
                        toDate,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ResponseApi.<PageResponse<CibilScoreRangeReportDto>>builder()
                        .success(true)
                        .message("CIBIL score range report fetched successfully.")
                        .data(report)
                        .build()
        );
    }


    // Report 4: Score Card Wise Report
    @GetMapping("/score-card-wise")
    public ResponseEntity<ResponseApi<PageResponse<ScoreCardWiseReportDto>>> getScoreCardWiseReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size
    ) {

        PageResponse<ScoreCardWiseReportDto> report =
                reportService.getScoreCardWiseReport(
                        fromDate, toDate, page, size
                );

        return ResponseEntity.ok(
                ResponseApi.<PageResponse<ScoreCardWiseReportDto>>builder()
                        .success(true)
                        .message("Score card wise report fetched successfully.")
                        .data(report)
                        .build()
        );
    }


    @GetMapping("/product-wise")
    public ResponseEntity<ResponseApi<PageResponse<ProductWiseReportDto>>> getProductWiseReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "5") int size
    ) {

        PageResponse<ProductWiseReportDto> report =
                reportService.getProductWiseReport(fromDate, toDate, page, size);

        return ResponseEntity.ok(
                ResponseApi.<PageResponse<ProductWiseReportDto>>builder()
                        .success(true)
                        .message("Product wise report fetched successfully.")
                        .data(report)
                        .build()
        );
    }



    @GetMapping("/turn-around-time")
    public ResponseEntity<ResponseApi<PageResponse<TurnAroundTimeReportDto>>> getTurnAroundTimeReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        PageResponse<TurnAroundTimeReportDto> report = reportService.getTurnAroundTimeReport(fromDate, toDate, page, size);

        return ResponseEntity.ok(
                ResponseApi.<PageResponse<TurnAroundTimeReportDto>>builder()
                        .success(true)
                        .message("Turnaround time (TAT) report fetched successfully.")
                        .data(report)
                        .build()
        );
    }
}
