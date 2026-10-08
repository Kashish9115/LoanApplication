package com.example.loanApplication.serviceImpl;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.EligibilityResultDto;
import com.example.loanApplication.entity.CibilReport;
import com.example.loanApplication.entity.Customer;
import com.example.loanApplication.entity.EligibilityResult;
import com.example.loanApplication.enumeration.EligibilityStatusEnum;
import com.example.loanApplication.repository.CibilReportRepository;
import com.example.loanApplication.repository.CustomerRepository;
import com.example.loanApplication.repository.EligibilityResultRepository;
import com.example.loanApplication.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {

    private final EligibilityResultRepository eligibilityResultRepository;
    private final CustomerRepository customerRepository;
    private final CibilReportRepository cibilReportRepository;

    private final ModelMapper modelMapper = new ModelMapper();


    @Override
    public ResponseApi<EligibilityResultDto> checkEligibility(Integer customerId) {

        log.info("Eligibility check started for customerId: {}", customerId);

        // 1. Get customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found with id: " + customerId
                        ));

        // 2. Get latest CIBIL report
        CibilReport cibilReport =
                cibilReportRepository
                        .findTopByCustomerIdOrderByCheckDateDesc(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "CIBIL report not found for customer: "
                                                + customerId
                                ));

        int cibilScore = cibilReport.getCibilScore();

        log.info(
                "CustomerId: {}, CIBIL score: {}",
                customerId,
                cibilScore
        );

        // 3. Create eligibility result
        EligibilityResult result = new EligibilityResult();

        result.setCustomerId(customerId);
        result.setCibilScore(cibilScore);


        if (cibilScore > 800) {

            result.setIsEligible(true);
            result.setStatus(EligibilityStatusEnum.APPROVED);

            result.setLoanAmount(
                    calculateLoanAmount(cibilScore)
            );

            result.setRejectionReason(null);

            log.info(
                    "CustomerId: {} AUTO APPROVED. CIBIL: {}",
                    customerId,
                    cibilScore
            );

        } else if (cibilScore < 650) {

            result.setIsEligible(false);
            result.setStatus(EligibilityStatusEnum.REJECTED);

            result.setLoanAmount(null);

            result.setRejectionReason(
                    "CIBIL score is below 650"
            );

            log.info(
                    "CustomerId: {} AUTO REJECTED. CIBIL: {}",
                    customerId,
                    cibilScore
            );

        } else {

            // 650 to 800 -> Officer needs to review

            result.setIsEligible(false);
            result.setStatus(EligibilityStatusEnum.PENDING);

            result.setLoanAmount(
                    calculateLoanAmount(cibilScore)
            );

            result.setRejectionReason(null);

            log.info(
                    "CustomerId: {} marked PENDING for manual review. CIBIL: {}",
                    customerId,
                    cibilScore
            );
        }

        // 4. Save result
        EligibilityResult savedResult =
                eligibilityResultRepository.save(result);

        // 5. Convert to response DTO
        EligibilityResultDto dto =
                mapToDto(savedResult, customer);

        return new ResponseApi<>(
                true,
                "Eligibility checked successfully",
                dto
        );
    }

    @Override
    public ResponseApi<EligibilityResultDto> approveEligibility(
            Integer eligibilityId) {

        log.info(
                "Manual approval started for eligibilityId: {}",
                eligibilityId
        );

        EligibilityResult result =
                eligibilityResultRepository.findById(eligibilityId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Eligibility result not found with id: "
                                                + eligibilityId
                                ));

        Customer customer =
                customerRepository.findById(result.getCustomerId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found with id: "
                                                + result.getCustomerId()
                                ));

        result.setIsEligible(true);
        result.setStatus(EligibilityStatusEnum.APPROVED);
        result.setRejectionReason(null);

        if (result.getLoanAmount() == null) {
            result.setLoanAmount(
                    calculateLoanAmount(result.getCibilScore())
            );
        }

        EligibilityResult savedResult =
                eligibilityResultRepository.save(result);

        log.info(
                "EligibilityId: {} manually approved",
                eligibilityId
        );

        return new ResponseApi<>(
                true,
                "Eligibility approved successfully",
                mapToDto(savedResult, customer)
        );
    }

    @Override
    public ResponseApi<EligibilityResultDto> rejectEligibility(
            Integer eligibilityId,
            String reason) {

        log.info(
                "Manual rejection started for eligibilityId: {}",
                eligibilityId
        );

        EligibilityResult result =
                eligibilityResultRepository.findById(eligibilityId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Eligibility result not found with id: "
                                                + eligibilityId
                                ));

        Customer customer =
                customerRepository.findById(result.getCustomerId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found with id: "
                                                + result.getCustomerId()
                                ));

        result.setIsEligible(false);
        result.setStatus(EligibilityStatusEnum.REJECTED);
        result.setLoanAmount(null);
        result.setRejectionReason(reason);

        EligibilityResult savedResult =
                eligibilityResultRepository.save(result);

        log.info(
                "EligibilityId: {} manually rejected",
                eligibilityId
        );

        return new ResponseApi<>(
                true,
                "Eligibility rejected successfully",
                mapToDto(savedResult, customer)
        );
    }

    @Override
    public ResponseApi<EligibilityResultDto> getCustomerEligibility(
            Integer customerId) {

        log.info(
                "Fetching eligibility result for customerId: {}",
                customerId
        );

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Customer not found with id: "
                                                + customerId
                                ));

        EligibilityResult result =
                eligibilityResultRepository
                        .findTopByCustomerIdOrderByEligibilityIdDesc(customerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Eligibility result not found for customer: "
                                                + customerId
                                ));

        return new ResponseApi<>(
                true,
                "Eligibility result fetched successfully",
                mapToDto(result, customer)
        );
    }

    @Override
    public ResponseApi<List<EligibilityResultDto>> getPendingCustomers() {

        List<EligibilityResult> pendingResults =
                eligibilityResultRepository
                        .findByStatus(EligibilityStatusEnum.PENDING);

        List<EligibilityResultDto> dtoList =
                pendingResults.stream()
                        .map(result -> {

                            Customer customer =
                                    customerRepository
                                            .findById(result.getCustomerId())
                                            .orElseThrow(() ->
                                                    new RuntimeException(
                                                            "Customer not found with id: "
                                                                    + result.getCustomerId()
                                                    ));

                            return mapToDto(result, customer);
                        })
                        .toList();

        return new ResponseApi<>(
                true,
                "Pending eligibility customers fetched successfully",
                dtoList
        );
    }



    private BigDecimal calculateLoanAmount(int cibilScore) {
        if (cibilScore >= 900) {
            return new BigDecimal("50000000");

        }

        if (cibilScore >= 800) {
            return new BigDecimal("10000000");

        }

        if (cibilScore >= 750) {
            return new BigDecimal("7500000");
        }

        if (cibilScore >= 700) {
            return new BigDecimal("5000000");
        }

        if (cibilScore >= 650) {
            return new BigDecimal("2500000");
        }

        return null;
    }

    private EligibilityResultDto mapToDto(
            EligibilityResult result,
            Customer customer) {

        EligibilityResultDto dto =
                modelMapper.map(result, EligibilityResultDto.class);

        dto.setCustomerName(
                customer.getFirstName() + " " + customer.getLastName()
        );

        dto.setPanNo(customer.getPanNo());

        dto.setMonthlyIncome(customer.getMonthlyIncome());

        return dto;
    }
}
