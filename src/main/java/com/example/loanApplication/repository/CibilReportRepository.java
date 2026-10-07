package com.example.loanApplication.repository;


import com.example.loanApplication.entity.CibilReport;

import java.util.Optional;

public interface CibilReportRepository {

//    CibilReport save(CibilReport cibilReport);

    Optional<CibilReport> findLatestByCustomerId(Integer customerId);
}
