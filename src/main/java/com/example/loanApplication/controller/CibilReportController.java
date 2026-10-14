package com.example.loanApplication.controller;

import com.example.loanApplication.dto.CibilReportDto;
import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.service.CibilReportService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CibilReportController {
    private  final CibilReportService cibilReportService;

    @PostMapping("/customer/{customerId}/cibilscore")
    public ResponseApi<CibilReportDto> genarteCibil( @PathVariable("customerId") Integer customerId){
        return  cibilReportService.generateCibil(customerId);
    }

    @GetMapping("/customer/{customerId}/cibilscore")
        public ResponseApi<CibilReportDto> getCibilScore (@PathVariable("customerId") Integer customerId){
            return cibilReportService.getLatestCibil(customerId);
        }


}
