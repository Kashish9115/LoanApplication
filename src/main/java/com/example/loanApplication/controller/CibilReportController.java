package com.example.loanApplication.controller;

import com.example.loanApplication.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class CibilReportController {
    private  final CibilReportService cibilReportService;

    @PostMapping("/customer/{customerId}/cibilscore")
    public Customer genarteCibil(@PathVariable int id){
        return  cibilReportService.saveCibil(id);
    }


}
