package com.example.loanApplication.serviceImpl;
import com.example.loanApplication.dto.CibilReportDto;
import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.entity.CibilReport;
import com.example.loanApplication.entity.Customer;
import com.example.loanApplication.repository.CibilReportRepository;
import com.example.loanApplication.repository.CustomerRepository;
import com.example.loanApplication.service.CibilReportService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
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

        Double monthlyIncome = customer.getMonthlyIncome();
        String employmentType = customer.getEmploymentType();
        Integer age = customer.getAge();
        Double monthlyInvest=customer.getMonthlyInvestment();

        Double foir = 0.0;

        if (monthlyIncome > 0) {
            foir = (monthlyInvest / monthlyIncome) * 100;
        }

        Integer cibilScore=900;
        CibilReport report = new CibilReport();
       report.setCustomerId(customerId);
        report.setPanNo(customer.getPanNo());
        report.setCibilScore(cibilScore);
        report.setCheckDate(LocalDateTime.now());
        report.setStatus("GENERATED");

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
                .findTopByCustomer_CustomerIdOrderByCheckDateDesc(customerId)
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

}
