package com.example.loanApplication.service;

import com.example.loanApplication.dto.CibilReportDto;
import com.example.loanApplication.apiResponse.ResponseApi;

public interface CibilReportService {

    ResponseApi<CibilReportDto> generateCibil(Integer customerId);

    ResponseApi<CibilReportDto> getLatestCibil(Integer customerId);


}