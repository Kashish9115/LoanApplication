package com.example.loanApplication.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationProgressDto {

    private Integer dealId;
    private String applicationId;
    private String dealStatus;
    private Integer activeStep;
    private String bannerMessage;
    private BigDecimal loanAmount;
    private List<StepperStepDto> steps;
}
