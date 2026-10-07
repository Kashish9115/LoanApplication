package com.example.loanApplication.serviceImpl;
import com.example.loanApplication.dto.CibilReportDto;
import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.entity.CibilReport;
import com.example.loanApplication.entity.Customer;
import com.example.loanApplication.enumeration.CibilSCoreEnum;
import com.example.loanApplication.repository.CibilReportRepository;
import com.example.loanApplication.repository.CustomerRepository;
import com.example.loanApplication.service.CibilReportService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CibilReportServiceImpl implements CibilReportService {

    private final CibilReportRepository cibilReportRepository;
    private final CustomerRepository customerRepository;
    ModelMapper modelMapper=new ModelMapper();
    @Override
    public ResponseApi<CibilReportDto> generateCibil(Integer customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found with id: " + customerId));


        BigDecimal monthlyIncome = customer.getMonthlyIncome();
        BigDecimal monthlyInvest = customer.getMonthlyInvestment();

        double income = monthlyIncome != null
                ? monthlyIncome.doubleValue()
                : 0.0;

        double investment = monthlyInvest != null
                ? monthlyInvest.doubleValue()
                : 0.0;
        String employmentType = customer.getEmploymentType();
        Integer age = customer.getAge();

        Double foir = 0.0;
        if (income > 0) {
            foir = (investment / income) * 100;
        }
        int incomeScore = calculateIncomeScore(income);

        int employmentScore = calculateEmploymentScore(employmentType);

        int ageScore = calculateAgeScore(age);

        int foirScore = calculateFoirScore(foir);


        int cibilScore =
                         incomeScore
                        + employmentScore
                        + ageScore
                        + foirScore;

        CibilSCoreEnum status = calculateCibilStatus(cibilScore);



        CibilReport report = new CibilReport();
       report.setCustomerId(customerId);
        report.setPanNo(customer.getPanNo());
        report.setCibilScore(cibilScore);
        report.setCheckDate(LocalDateTime.now());
        report.setStatus(String.valueOf(status));

        CibilReport savedReport = cibilReportRepository.save(report);


       CibilReportDto dto= modelMapper.map(savedReport,CibilReportDto.class);

        return new ResponseApi<CibilReportDto>(
                true,
                "CIBIL score generated successfully",
                dto
        );
    }

    @Override
    public ResponseApi<CibilReportDto> getLatestCibil(Integer customerId) {

        CibilReport report = cibilReportRepository
                .findTopByCustomerIdOrderByCheckDateDesc(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "CIBIL report not found for customer: " + customerId
                        ));

        CibilReportDto dto = modelMapper.map(report,CibilReportDto.class);

        return new ResponseApi<>(
                true,
                "CIBIL report fetched successfully",
                dto
        );
    }


    private int calculateIncomeScore(double income) {

        if (income < 25000) {
            return 100;

        } else if (income>=25000 &&income<50000) {
            return 200;

        } else if (income>=50000 && income<100000) {
            return 300;

        } else if(income>=100000) {
            return 600;
        }
        else {
            return 0;
        }
    }


    private int calculateEmploymentScore(String employmentType) {

        if (employmentType == null) {
            return 0;
        }

        switch (employmentType.toLowerCase()) {

            case "government":
                return 200;

            case "private":
                return 150;

            case "self":
            case "self employed":
                return 100;

            default:
                return 0;
        }
    }


    private int calculateAgeScore(int age) {

        if (age >= 21 && age <= 24) {
            return 50;

        } else if (age >= 25 && age <= 45) {
            return 150;

        } else if (age >= 46 && age <= 60) {
            return 100;

        } else {
            return 0;
        }
    }


    private int calculateFoirScore(double foir) {

        if (foir < 30) {
            return 250;
        }

        if (foir >= 30 && foir < 50) {
            return 150;
        }

        if (foir >= 50 && foir < 60) {
            return 75;
        }

        return 0;
    }
    private CibilSCoreEnum calculateCibilStatus(int cibilScore) {

        if (cibilScore > 900) {
            return CibilSCoreEnum.Excellent;

        } else if (cibilScore >= 800 && cibilScore <= 899) {
            return CibilSCoreEnum.VeryGood;

        } else if (cibilScore >= 750 && cibilScore <= 799) {
            return CibilSCoreEnum.Good;

        } else if (cibilScore >= 700 && cibilScore <= 749) {
            return CibilSCoreEnum.Average;

        } else if (cibilScore >= 650 && cibilScore <= 699) {
            return CibilSCoreEnum.Risky;

        } else {
            return CibilSCoreEnum.Reject;
        }
    }


}
