package com.example.loanApplication.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;



@Data
public class KycBulkVerificationRequest {
    @NotEmpty
    @Size(max = 500, message = "At most 500 document IDs can be processed per request")
    private List<Integer> documentIds;
    @NotBlank
    private String verificationStatus;
    @Size(max = 1000)
    private String rejectionReason;
}
