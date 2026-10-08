package com.example.loanApplication.controller;

import com.example.loanApplication.apiResponse.ResponseApi;
import com.example.loanApplication.dto.EligibilityResultDto;
import com.example.loanApplication.dto.RejectEligibilityDto;
import com.example.loanApplication.service.EligibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EligibilityController {
     private  final EligibilityService eligibilityService;

     @GetMapping("/cred-officer/eligibilty/pending")
    public ResponseApi<List<EligibilityResultDto>> getPendingCustomers(){
         return eligibilityService.getPendingCustomers();
     }

     @PostMapping("/cred-officer/customer/{customerId}/eligibility")
    public  ResponseApi<EligibilityResultDto> checkEligible(
            @PathVariable("customerId") Integer customerId
     ){
         return  eligibilityService.checkEligibility(customerId);
     }
//
//     @PutMapping("cred-officer/eligibilty/{eligibiltyId/reject")
//     public ResponseApi<EligibilityResultDto> approveEligibility(
//             @PathVariable("eligibilityId") Integer eligibilityId) {
//
//         return eligibilityService.approveEligibility(eligibilityId);
//     }
//
//    // Loan Officer - manually approve pending eligibility
//    @PutMapping("/loan-officer/eligibility/{eligibilityId}/approve")
//    public ResponseApi<EligibilityResultDto> approveEligibility(
//            @PathVariable("eligibilityId") Integer eligibilityId) {
//
//        return eligibilityService.approveEligibility(eligibilityId);
//    }

    // Loan Officer - manually reject pending eligibility
    @PutMapping("/loan-officer/eligibility/{eligibilityId}/reject")
    public ResponseApi<EligibilityResultDto> rejectEligibility(
            @PathVariable("eligibilityId") Integer eligibilityId,
            @RequestBody RejectEligibilityDto request) {

        return eligibilityService.rejectEligibility(
                eligibilityId,
                request.getReason()
        );
    }

    // Customer - view final eligibility result
    @GetMapping("/customer/{customerId}/eligibility")
    public ResponseApi<EligibilityResultDto> getCustomerEligibility(
            @PathVariable("customerId") Integer customerId) {

        return eligibilityService.getCustomerEligibility(customerId);
    }
}
